package mailo
package http

import com.typesafe.scalalogging.LazyLogging

import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.{Authorization, BasicHttpCredentials, RawHeader}
import akka.http.scaladsl.marshalling._
import akka.http.scaladsl.unmarshalling._

import akka.stream.scaladsl.Source
import akka.actor.ActorSystem

import scala.concurrent.{ExecutionContext, Future}
import javax.mail.Message.RecipientType
import javax.mail.internet.MimeMessage

import com.typesafe.config.{Config, ConfigFactory}

import cats.data.EitherT
import cats.syntax.either._

import akka.http.scaladsl.model.ContentType
import akka.util.ByteString

import util._

class MailgunClient(implicit
  system: ActorSystem,
  conf: Config = ConfigFactory.load(),
) extends MailClient
    with MimeMailClient
    with LazyLogging {
  import MailClientError._
  import mailo.MailRefinedContent._

  private[this] case class MailgunConfig(key: String, uri: String)
  private[this] val mailgunConfig = MailgunConfig(
    key = conf.getString("mailo.mailgun.key"),
    uri = conf.getString("mailo.mailgun.uri"),
  )
  private[this] val auth = Authorization(BasicHttpCredentials("api", mailgunConfig.key))

  def sendMime(
    message: MimeMessage,
    tags: List[String] = List.empty,
    attachments: List[Attachment] = List.empty,
    headers: Map[String, String] = Map.empty,
  )(implicit
    executionContext: ExecutionContext,
  ): Future[Either[MailError, MailResponse]] = {
    val inputs = for {
      toRecipients <- message.getRecipients(RecipientType.TO) match {
        case addresses if addresses.nonEmpty => addresses.asRight
        case _                               => InvalidInput("No recipients in MimeMessage").asLeft
      }
      _ <- message.getRecipients(RecipientType.CC).asRight
      _ <- message.getRecipients(RecipientType.BCC).asRight
      _ <- Either.fromOption(
        Option(message.getFrom()),
        InvalidInput("No 'from' in MimeMessage"): MailError,
      )
      _ <- Either.fromOption(
        Option(message.getSubject()),
        InvalidInput("No 'subject' in MimeMessage"): MailError,
      )
    } yield {
      val to = toRecipients.map(_.toString).mkString(",")
      val mimeMessage = {
        val out = new java.io.ByteArrayOutputStream
        message.writeTo(out)
        out.toString
      }
      (to, mimeMessage)
    }

    (for {
      inputs <- EitherT.fromEither[Future](inputs)
      (to, message) = inputs
      entity <- EitherT.liftF(mimeEntity(to, message, tags, headers, attachments))
      request = HttpRequest(
        method = HttpMethods.POST,
        uri = s"${mailgunConfig.uri}/messages.mime",
        headers = List(auth),
        entity = entity,
      )
      res <- EitherT(sendRequest(request))
    } yield res).value
  }

  def sendBatch(
    from: String,
    cc: Option[String],
    bcc: Option[String],
    subject: String,
    content: MailRefinedContent,
    attachments: List[Attachment],
    tags: List[String],
    recipientVariables: Map[String, Map[String, String]],
    headers: Map[String, String],
  )(implicit
    executionContext: ExecutionContext,
  ): Future[Either[MailError, MailResponse]] =
    for {
      entity <- batchEntity(
        from = from,
        cc = cc,
        bcc = bcc,
        subject = subject,
        content = content,
        attachments = attachments,
        tags = tags,
        recipientVariables = recipientVariables,
        headers = headers,
      )
      request = HttpRequest(
        method = HttpMethods.POST,
        uri = s"${mailgunConfig.uri}/messages",
        headers = List(auth),
        entity = entity,
      )
      res <- sendRequest(request)
    } yield res

  def send(
    to: String,
    from: String,
    cc: Option[String] = None,
    bcc: Option[String] = None,
    subject: String,
    content: MailRefinedContent,
    attachments: List[Attachment],
    tags: List[String],
    headers: Map[String, String] = Map.empty,
  )(implicit
    executionContext: scala.concurrent.ExecutionContext,
  ): Future[Either[MailError, MailResponse]] =
    for {
      entity <- entity(
        from = from,
        to = to,
        cc = cc,
        bcc = bcc,
        subject = subject,
        content = content,
        attachments = attachments,
        tags = tags,
        headers = headers,
      )
      request = HttpRequest(
        method = HttpMethods.POST,
        uri = s"${mailgunConfig.uri}/messages",
        headers = List(auth),
        entity = entity,
      )
      res <- sendRequest(request)
    } yield res

  private[this] def sendRequest(request: HttpRequest)(implicit
    ec: ExecutionContext,
  ): Future[Either[MailError, MailResponse]] = {
    import de.heikoseeberger.akkahttpcirce.ErrorAccumulatingCirceSupport._
    import io.circe.generic.auto._

    (for {
      response <- Http().singleRequest(request)
      _ = logger.debug(s"response from server: $response")
      result <- response.status.intValue match {
        case 200 =>
          Unmarshal(response.entity).to[MailResponse].map(_.asRight[MailError])
        case 400                   => Future(BadRequest.asLeft[MailResponse])
        case 401                   => Future(Unauthorized.asLeft[MailResponse])
        case 402                   => Future(RequestFailed.asLeft[MailResponse])
        case 404                   => Future(NotFound.asLeft[MailResponse])
        case 500 | 502 | 503 | 504 => Future(ServerError.asLeft[MailResponse])
        case _                     => Future(UnknownCode.asLeft[MailResponse])
      }
    } yield result).recover { case t: Throwable =>
      UnknownError(t.getStackTraceAsString).asLeft[MailResponse]
    }
  }

  private[this] def attachmentForm(
    name: String,
    `type`: ContentType,
    content: String,
    transferEncoding: Option[String],
  ) = {
    Multipart.FormData.BodyPart.Strict(
      name = "attachment",
      entity = HttpEntity(`type`, ByteString(content)),
      additionalDispositionParams = Map("filename" -> name),
      additionalHeaders = transferEncoding match {
        case Some(e) => List(RawHeader("Content-Transfer-Encoding", e))
        case None    => Nil
      },
    )
  }

  private[this] def tagsForm(tags: List[String]) =
    tags.map(Multipart.FormData.BodyPart.Strict("o:tag", _))

  private[this] def headersForm(headers: Map[String, String]) =
    headers.map { case (k, v) => Multipart.FormData.BodyPart.Strict(s"h:$k", v) }

  private[this] def mimeEntity(
    to: String,
    message: String,
    tags: List[String],
    headers: Map[String, String],
    attachments: List[Attachment],
  )(implicit ec: ExecutionContext): Future[RequestEntity] = {

    val attachmentsForm = attachments.map(attachment =>
      attachmentForm(
        attachment.name,
        attachment.`type`,
        attachment.content,
        attachment.transferEncoding,
      ),
    )

    val multipartForm = Multipart.FormData(
      Source(
        List(
          Multipart.FormData.BodyPart.Strict(
            name = "message",
            entity = HttpEntity(ContentTypes.`text/plain(UTF-8)`, ByteString(message)),
            additionalDispositionParams = Map("filename" -> "message"),
          ),
          Multipart.FormData.BodyPart.Strict("to", to),
        ) ++ tagsForm(tags) ++ headersForm(headers) ++ attachmentsForm,
      ),
    )

    Marshal(multipartForm).to[RequestEntity]
  }

  private[this] def entity(
    from: String,
    to: String,
    cc: Option[String],
    bcc: Option[String],
    subject: String,
    content: MailRefinedContent,
    attachments: List[Attachment],
    tags: List[String],
    headers: Map[String, String],
  )(implicit
    executionCon: scala.concurrent.ExecutionContext,
  ): Future[RequestEntity] = {
    import mailo.MailRefinedContent._

    val contentForm = content match {
      case HTMLContent(html) => Multipart.FormData.BodyPart.Strict("html", html)
      case TEXTContent(text) => Multipart.FormData.BodyPart.Strict("text", text)
    }

    val attachmentsForm = attachments.map(attachment =>
      attachmentForm(
        attachment.name,
        attachment.`type`,
        attachment.content,
        attachment.transferEncoding,
      ),
    )

    val multipartForm = Multipart.FormData(
      Source(
        List(
          Multipart.FormData.BodyPart.Strict("from", from),
          Multipart.FormData.BodyPart.Strict("to", to),
          Multipart.FormData.BodyPart.Strict("subject", subject),
        ) ++ List(
          cc.map(Multipart.FormData.BodyPart.Strict("cc", _)),
          bcc.map(Multipart.FormData.BodyPart.Strict("bcc", _)),
        ).flatten ++ tagsForm(tags) ++ attachmentsForm ++ headersForm(headers) :+ contentForm,
      ),
    )

    Marshal(multipartForm).to[RequestEntity]
  }

  private[this] def batchEntity(
    from: String,
    cc: Option[String],
    bcc: Option[String],
    subject: String,
    content: MailRefinedContent,
    attachments: List[Attachment],
    tags: List[String],
    recipientVariables: Map[String, Map[String, String]],
    headers: Map[String, String],
  )(implicit
    executionCon: scala.concurrent.ExecutionContext,
  ): Future[RequestEntity] = {
    import mailo.MailRefinedContent._
    import io.circe.syntax._

    val contentForm = content match {
      case HTMLContent(html) => Multipart.FormData.BodyPart.Strict("html", html)
      case TEXTContent(text) => Multipart.FormData.BodyPart.Strict("text", text)
    }

    val recipientVariablesEntity =
      HttpEntity(ContentTypes.`application/json`, ByteString(recipientVariables.asJson.noSpaces))
    val recipientVariablesForm =
      Multipart.FormData.BodyPart.Strict("recipient-variables", recipientVariablesEntity)
    val tos = recipientVariables.map { case (to, _) =>
      Multipart.FormData.BodyPart.Strict("to", to)
    }

    val attachmentsForm = attachments.map(attachment =>
      attachmentForm(
        attachment.name,
        attachment.`type`,
        attachment.content,
        attachment.transferEncoding,
      ),
    )

    val multipartForm = Multipart.FormData(
      Source(
        List(
          Multipart.FormData.BodyPart.Strict("from", from),
          Multipart.FormData.BodyPart.Strict("subject", subject),
          recipientVariablesForm,
        ) ++ List(
          cc.map(Multipart.FormData.BodyPart.Strict("cc", _)),
          bcc.map(Multipart.FormData.BodyPart.Strict("bcc", _)),
        ).flatten ++ tagsForm(tags) ++ tos ++ attachmentsForm ++ headersForm(headers) :+ contentForm,
      ),
    )

    Marshal(multipartForm).to[RequestEntity]
  }

}
