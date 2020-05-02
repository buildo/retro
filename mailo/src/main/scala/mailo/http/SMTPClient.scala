package mailo.http

import java.util.{Properties, UUID}

import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.LazyLogging
import javax.mail.internet.{InternetAddress, MimeMessage}
import mailo.{Attachment, MailError, MailRefinedContent, MailResponse}

import scala.concurrent.{ExecutionContext, Future}
import scala.util._
import javax.mail._
import mailo.MailRefinedContent.{HTMLContent, TEXTContent}
import mailo.http.MailClientError.{BadRequest, UnknownError}
import scala.util.control.NonFatal

class SMTPClient(implicit conf: Config = ConfigFactory.load())
    extends MailClient
    with MimeMailClient
    with LazyLogging {

  lazy val server = conf.getString("mailo.smtp.server")
  lazy val port = conf.getString("mailo.smtp.port")

  private[this] def internalSend(message: MimeMessage) =
    Try(Transport.send(message, message.getAllRecipients)) match {
      case Success(_) => Future.successful(Right(MailResponse(UUID.randomUUID().toString, "ok")))
      case Failure(NonFatal(exception)) =>
        Future.successful(Left(UnknownError(exception.getMessage)))
    }

  //attachments - tags guard
  private[this] def internalSend(
    message: MimeMessage,
    attachments: List[Attachment],
    tags: List[String],
  ): Future[Either[MailError, MailResponse]] = {
    if (attachments.nonEmpty) {
      logger.error("attachments with smtp not yet supported")
      Future.successful(Left(BadRequest))
    } else if (tags.nonEmpty) {
      logger.error("tags with smtp not yet supported")
      Future.successful(Left(BadRequest))
    } else internalSend(message)
  }

  private[this] def send(
    from: InternetAddress,
    recipients: String,
    subject: String,
    content: MailRefinedContent.MailRefinedContent,
    attachments: List[Attachment],
    tags: List[String],
    headers: Map[String, String],
  ) = {
    val props = new Properties
    props.put("mail.smtp.host", server)
    props.put("mail.smtp.port", port)
    props.put("mail.smtp.auth", "false")
    val session = Session.getInstance(props, null)
    val msg = new MimeMessage(session) {
      setFrom(from)
      setRecipients(Message.RecipientType.TO, recipients)
      setSubject(subject)
      setSentDate(java.util.Date.from(java.time.Instant.now))
    }

    content match {
      case HTMLContent(html) => msg.setContent(html, "text/html; charset=utf-8")
      case TEXTContent(text) => msg.setText(text)
    }

    headers.foreach(h => msg.addHeader(h._1, h._2))
    internalSend(msg, attachments, tags)
  }

  override def send(
    to: String,
    from: String,
    cc: Option[String],
    bcc: Option[String],
    subject: String,
    content: MailRefinedContent.MailRefinedContent,
    attachments: List[Attachment],
    tags: List[String],
    headers: Map[String, String],
  )(
    implicit executionContext: ExecutionContext,
  ): Future[
    Either[MailError, MailResponse],
  ] = {
    val recipients = to + cc.map(a => s"; $a").getOrElse("")
    //handling addresses parsing like mailo wants it
    (for {
      from <- Try(new InternetAddress(from))
    } yield send(from, recipients, subject, content, attachments, tags, headers)).getOrElse(
      Future.successful(Left(BadRequest)),
    )
  }

  override def sendMime(
    message: MimeMessage,
    tags: List[String],
    attachments: List[Attachment],
    headers: Map[String, String],
  )(
    implicit executionContext: ExecutionContext,
  ): Future[
    Either[MailError, MailResponse],
  ] = {
    headers.foreach(h => message.addHeader(h._1, h._2))
    internalSend(message, attachments, tags)
  }

  override def sendBatch(
    to: List[String],
    from: String,
    cc: Option[String],
    bcc: Option[String],
    subject: String,
    content: MailRefinedContent.MailRefinedContent,
    attachments: List[Attachment],
    tags: List[String],
    recipientVariables: Map[String, Map[String, String]],
    headers: Map[String, String]
  )(
    implicit
    executionContext: ExecutionContext
  ): Future[Either[MailError, MailResponse]] =
    throw new UnsupportedOperationException("unable to send batch messages in SMTP")
}
