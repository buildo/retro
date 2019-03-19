package mailo.http

import java.util.{Properties, UUID}
import com.typesafe.config.{Config, ConfigFactory}
import javax.mail.internet.{InternetAddress, MimeMessage}
import mailo.{Attachment, MailError, MailRefinedContent, MailResponse}

import scala.concurrent.{ExecutionContext, Future}
import scala.util._
import javax.mail._
import mailo.http.MailClientError.{UnknownError}

class SmtpClient(implicit conf: Config = ConfigFactory.load()) extends MailClient with MimeMailClient {

  val server = "0.0.0.0"
  val port = "1025"

  private [this] def internalSend(
                           message: MimeMessage,
                           addresses: Array[Address],
                           attachments: List[Attachment],
                           tags: List[String]): Future[Either[MailError, MailResponse]] = {
    if(attachments != Nil) throw new NotImplementedError("attachments with smtp not yet supported")
    if(tags != Nil) throw new NotImplementedError("tags with smtp not yet supported")

    Try(Transport.send(message, message.getAllRecipients)) match {
      case Success(_) => Future.successful(Right(MailResponse(UUID.randomUUID().toString, "ok")))
      case Failure(exception : SendFailedException) => Future.successful(Left(UnknownError(exception.getMessage)))
      case Failure(exception : MessagingException) => Future.successful(Left(UnknownError(exception.getMessage)))
    }
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
      headers: Map[String, String]
    )(
      implicit executionContext: ExecutionContext
    ): Future[
      Either[MailError, MailResponse]
    ] = {
    val props = new Properties
    props.put("mail.smtp.host", server)
    props.put("mail.smtp.port", port)
    props.put("mail.smtp.auth", "false")

    val session = Session.getInstance(props, null)
    val msg = new MimeMessage(session)
    msg.setFrom(new InternetAddress(from))
    msg.setRecipients(Message.RecipientType.TO, to + cc.map(a => s"; $a").getOrElse(""))
    msg.setSubject(subject)
    msg.setSentDate(java.util.Date.from(java.time.Instant.now))
    msg.setText(content.content)
    headers.foreach(h => msg.addHeader(h._1, h._2))
    val addresses : Array[Address] = Array(
      Some(to),
      cc,
      bcc
    ).flatMap(_.map(a => new InternetAddress(a)))

    internalSend(msg, addresses, attachments, tags)
  }

  override def sendMime(
      message: MimeMessage,
      tags: List[String],
      attachments: List[Attachment],
      headers: Map[String, String]
    )(
      implicit executionContext: ExecutionContext
    ): Future[
      Either[MailError, MailResponse]
    ] = {
    headers.foreach(h => message.addHeader(h._1, h._2))
    internalSend(message, message.getAllRecipients, attachments, tags)
  }
}
