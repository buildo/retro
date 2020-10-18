package mailo
package http

import com.typesafe.scalalogging.LazyLogging

import com.typesafe.config.{Config, ConfigFactory}

import cats.syntax.either._

import sendinblue._
import sendinblue.auth._
import sibModel._
import sibApi.SmtpApi

import MailClientError._
import MailRefinedContent._

import scala.concurrent.{ExecutionContext, Future}

import javax.mail.internet.MimeMessage

class SendinblueClient(implicit
  conf: Config = ConfigFactory.load(),
) extends MailClient
    with MimeMailClient
    with LazyLogging {

  private[this] case class SendinblueConfig(key: String)
  private[this] val sendinblueConfig = SendinblueConfig(
    key = conf.getString("mailo.sendinblue.key"),
  )
  private[this] val client = Configuration.getDefaultApiClient()
  private[this] val apiKey = client.getAuthentication("api-key").asInstanceOf[ApiKeyAuth]
  apiKey.setApiKey(sendinblueConfig.key)
  private[this] val sendinblue = new SmtpApi()

  case class SendinblueResponse(code: String, message: String, data: SendinblueResponseData)
  case class SendinblueResponseData(`message-id`: Option[String])

  def sendMime(
    message: MimeMessage,
    tags: List[String] = List.empty,
    attachments: List[Attachment] = List.empty,
    headers: Map[String, String] = Map.empty,
  )(implicit
    executionContext: ExecutionContext,
  ): Future[Either[MailError, MailResponse]] =
    throw new UnsupportedOperationException("unable to send mime messages in Sendinblue")

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
    throw new UnsupportedOperationException("unable to send batch messages in Sendinblue")

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
        subject = subject,
        content = content,
        attachments = attachments,
        tags = tags,
      )
      res <- Future(sendinblue.sendTransacEmail(entity)).map { r =>
        MailResponse(r.getMessageId(), "Email sent successfully.").asRight[MailError]
      }.recover {
        case e: ApiException =>
          UnknownError(e.getResponseBody()).asLeft[MailResponse]
        case e: Throwable =>
          UnknownError(e.toString()).asLeft[MailResponse]
      }
    } yield res

  private[this] def entity(
    from: String,
    to: String,
    subject: String,
    content: MailRefinedContent,
    attachments: List[Attachment],
    tags: List[String],
  )(implicit ec: ExecutionContext): Future[SendSmtpEmail] = Future {
    import mailo.MailRefinedContent._
    import collection.JavaConverters._

    val (fromEmail, fromName) = {
      val splits = from.split(" ").toList
      (splits.last, splits.reverse.drop(1).reverse.mkString(" "))
    }
    val (html, text) = content match {
      case HTMLContent(html) => (html, null)
      case TEXTContent(text) => (null, text)
    }
    val email = new SendSmtpEmail()

    val sender = new SendSmtpEmailSender()
    sender.setEmail(fromEmail)
    sender.setName(fromName)

    val toM = new SendSmtpEmailTo()
    toM.setEmail(to)

    email.setSender(sender)
    email.setTo(List(toM).asJava)
    email.setSubject(subject)
    email.setTags(tags.asJava)
    email.setHtmlContent(html)
    email.setTextContent(text)
    if (attachments.size > 0) {
      email.setAttachment(attachments.map { a =>
        val aa = new SendSmtpEmailAttachment()
        aa.setContent(java.util.Base64.getDecoder.decode(a.content))
        aa.setName(a.name)
        aa
      }.asJava)
    }
    email
  }
}
