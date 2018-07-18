package mailo
package http

import com.typesafe.scalalogging.LazyLogging

import akka.stream.ActorMaterializer
import akka.actor.ActorSystem

import com.typesafe.config.{Config, ConfigFactory}

import cats.syntax.either._

import com.sendinblue.Sendinblue

import io.circe._, io.circe.generic.auto._, io.circe.parser.decode

import MailClientError._
import MailRefinedContent._

import scala.concurrent.{Future, ExecutionContext}

import javax.mail.internet.MimeMessage

class SendinblueClient(
  implicit
  system: ActorSystem,
  materializer: ActorMaterializer,
  conf: Config = ConfigFactory.load()
) extends MailClient
    with MimeMailClient
    with LazyLogging {

  private[this] case class SendinblueConfig(key: String)
  private[this] val sendinblueConfig = SendinblueConfig(
    key = conf.getString("mailo.sendinblue.key")
  )
  private[this] val sendinblue = new Sendinblue("https://api.sendinblue.com/v2.0", sendinblueConfig.key)

  case class SendinblueResponse(code: String, message: String, data: SendinblueResponseData)
  case class SendinblueResponseData(`message-id`: Option[String])

  def sendMime(
    message: MimeMessage,
    tags: List[String] = List.empty,
    attachments: List[Attachment] = List.empty,
    headers: Map[String, String] = Map.empty
  )(
    implicit
    executionContext: ExecutionContext
  ): Future[Either[MailError, MailResponse]] =
    throw new UnsupportedOperationException("unable to send mime messages in Sendinblue")


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
        headers = headers
      )
      res <- Future(sendinblue.send_email(entity))
      jsonRes = decode[SendinblueResponse](res)
    } yield {
      jsonRes match {
        case Right(SendinblueResponse(code, message, SendinblueResponseData(Some(messageId)))) if code == "success" =>
          MailResponse(messageId, message).asRight[MailError]
        case _ =>
          UnknownCode.asLeft[MailResponse]
      }
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
  )(implicit ec: ExecutionContext): Future[java.util.HashMap[String, Any]] = Future {
    import mailo.MailRefinedContent._

    val data = new java.util.HashMap[String, Any]()

    val toMap = new java.util.HashMap[String, String]()
    toMap.put(to, "")
    data.put("to", toMap)

    data.put("from", from.split(" ").toList.reverse.toArray)

    data.put("subject", subject)

    val headersMap = new java.util.HashMap[String, String]()
    if (tags.size > 0)
      headersMap.put("X-Mailin-Tag", tags.mkString(", "))
    headers.keys.foreach { k =>
      headersMap.put(k, headers(k))
    }
    data.put("headers", headers)


    val attachmentsMap = new java.util.HashMap[String, String]()
    attachments.foreach { a =>
      attachmentsMap.put(a.name, a.content)
    }
    data.put("attachment", attachmentsMap)

    content match {
      case HTMLContent(html) => data.put("html", html)
      case TEXTContent(text) => data.put("html", ""); data.put("text", text)
    }

    data
  }
}
