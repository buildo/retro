package mailo
package http

import com.typesafe.scalalogging.LazyLogging

import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.{Authorization, BasicHttpCredentials, RawHeader}
import akka.http.scaladsl.marshalling._
import akka.http.scaladsl.unmarshalling._

import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import akka.actor.ActorSystem

import scala.concurrent.Future

import com.typesafe.config.{Config, ConfigFactory}

import cats.syntax.either._

import akka.http.scaladsl.model.ContentType
import akka.util.ByteString

import util._

class MailgunClient(
  implicit
  system: ActorSystem,
  materializer: ActorMaterializer,
  conf: Config = ConfigFactory.load()
) extends MailClient
    with LazyLogging {
  import MailClientError._
  import mailo.MailRefinedContent._
  import mailo.MailResponse

  private[this] case class MailgunConfig(key: String, uri: String)
  private[this] val mailgunConfig = MailgunConfig(
    key = conf.getString("mailo.mailgun.key"),
    uri = conf.getString("mailo.mailgun.uri")
  )

  def send(
    to: String,
    from: String,
    cc: Option[String] = None,
    bcc: Option[String] = None,
    subject: String,
    content: MailRefinedContent,
    attachments: List[Attachment],
    tags: List[String],
    headers: Map[String, String] = Map.empty
  )(
    implicit
    executionContext: scala.concurrent.ExecutionContext
  ): Future[Either[MailError, MailResponse]] = {
    import de.heikoseeberger.akkahttpcirce.ErrorAccumulatingCirceSupport._
    import io.circe.generic.auto._

    val auth = Authorization(BasicHttpCredentials("api", mailgunConfig.key))

    val res = (for {
      entity <- entity(
        from = from,
        to = to,
        cc = cc,
        bcc = bcc,
        subject = subject,
        content = content,
        attachments = attachments,
        tags = tags,
        headers = headers
      )
      request = HttpRequest(
        method = HttpMethods.POST,
        uri = s"${mailgunConfig.uri}/messages",
        headers = List(auth),
        entity = entity
      )
      response <- Http().singleRequest(request)
      _ = logger.debug(s"response from server: $response")
      result <- response.status.intValue match {
        case 200 =>
          Unmarshal(response.entity).to[MailResponse].map(_.asRight[MailError])
        case 400 => Future(BadRequest.asLeft[MailResponse])
        case 401 => Future(Unauthorized.asLeft[MailResponse])
        case 402 => Future(RequestFailed.asLeft[MailResponse])
        case 404 => Future(NotFound.asLeft[MailResponse])
        case 500 | 502 | 503 | 504 => Future(ServerError.asLeft[MailResponse])
        case _ => Future(UnknownCode.asLeft[MailResponse])
      }
    } yield result).recover {
      case t: Throwable =>
        UnkownError(t.getStackTraceAsString).asLeft[MailResponse]
    }

    res
  }

  private[this] def attachmentForm(
    name: String,
    `type`: ContentType,
    content: String,
    transferEncoding: Option[String] = None
  ) = {
    Multipart.FormData.BodyPart.Strict(
      name = "attachment",
      entity = HttpEntity(`type`, ByteString(content)),
      additionalDispositionParams = Map("filename" -> name),
      additionalHeaders = transferEncoding match {
        case Some(e) => List(RawHeader("Content-Transfer-Encoding", e))
        case None => Nil
      }
    )
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
    headers: Map[String, String]
  )(
    implicit
    executionCon: scala.concurrent.ExecutionContext
  ): Future[RequestEntity] = {
    import mailo.MailRefinedContent._
    val tagsForm = tags.map(Multipart.FormData.BodyPart.Strict("o:tag", _))

    val contentForm = content match {
      case HTMLContent(html) => Multipart.FormData.BodyPart.Strict("html", html)
      case TEXTContent(text) => Multipart.FormData.BodyPart.Strict("text", text)
    }

    val attachmentsForm = attachments.map(
      attachment =>
        attachmentForm(
          attachment.name,
          attachment.`type`,
          attachment.content,
          attachment.transferEncoding
      )
    )

    val headersForm = headers.map {
      case (k, v) =>
        Multipart.FormData.BodyPart.Strict(s"h:$k", v)
    }

    val multipartForm = Multipart.FormData(
      Source(
        List(
          Multipart.FormData.BodyPart.Strict("from", from),
          Multipart.FormData.BodyPart.Strict("to", to),
          Multipart.FormData.BodyPart.Strict("subject", subject)
        ) ++ List(
          cc.map(Multipart.FormData.BodyPart.Strict("cc", _)),
          bcc.map(Multipart.FormData.BodyPart.Strict("bcc", _))
        ).flatten ++ tagsForm ++ attachmentsForm ++ headersForm :+ contentForm
      )
    )

    Marshal(multipartForm).to[RequestEntity]
  }
}
