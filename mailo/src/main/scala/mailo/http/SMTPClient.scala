package mailo.http

import java.util.{Properties, UUID}

import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.LazyLogging
import javax.mail.internet.{InternetAddress, MimeMessage}
import mailo.{Attachment, MailError, MailRefinedContent, MailResponse}

import scala.concurrent.{ExecutionContext, Future}
import scala.util._
import javax.mail._
import mailo.http.MailClientError.{BadRequest, UnknownError}

class SMTPClient(implicit conf: Config = ConfigFactory.load()) extends MailClient with MimeMailClient with LazyLogging{

  lazy val server = conf.getString("mailo.smtp.server")
  lazy val port = conf.getString("mailo.smtp.port")

  private [this] def internalSend(message: MimeMessage, addresses: Array[Address]) =
    Try(Transport.send(message, message.getAllRecipients)) match {
      case Success(_) => Future.successful(Right(MailResponse(UUID.randomUUID().toString, "ok")))
      case Failure(exception: SendFailedException) =>
        Future.successful(Left(UnknownError(exception.getMessage)))
      case Failure(exception: MessagingException) =>
        Future.successful(Left(UnknownError(exception.getMessage)))
    }

  //attachments - tags guard
  private [this] def internalSend(
                           message: MimeMessage,
                           addresses: Array[Address],
                           attachments: List[Attachment],
                           tags: List[String]): Future[Either[MailError, MailResponse]] = {
    if (attachments.nonEmpty) {
      logger.error("attachments with smtp not yet supported")
      Future.successful(Left(BadRequest))
    }
    else if (tags.nonEmpty) {
      logger.error("tags with smtp not yet supported")
      Future.successful(Left(BadRequest))
    }
    else internalSend(message, addresses)
  }

  private[this] def send(from: InternetAddress,
                         to : Array[Address],
                         recipients: String,
                         subject: String,
                         content: MailRefinedContent.MailRefinedContent,
                         attachments: List[Attachment],
                         tags: List[String],
                         headers: Map[String, String]) = {
    val props = new Properties
    props.put("mail.smtp.host", server)
    props.put("mail.smtp.port", port)
    props.put("mail.smtp.auth", "false")
    val session = Session.getInstance(props, null)
    val msg = new MimeMessage(session)
    msg.setFrom(from)
    msg.setRecipients(Message.RecipientType.TO, recipients)
    msg.setSubject(subject)
    msg.setSentDate(java.util.Date.from(java.time.Instant.now))
    msg.setText(content.content)
    headers.foreach(h => msg.addHeader(h._1, h._2))
    internalSend(msg, to, attachments, tags)
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
    val recipients = to + cc.map(a => s"; $a").getOrElse("")
    //handling addresses parsing like mailo wants it
    (for{
      from <- Try(new InternetAddress(from))
      to <- Try(Array(Some(to),cc,bcc).flatMap(_.map(a => new InternetAddress(a).asInstanceOf[Address])))
    } yield send(from, to, recipients, subject, content, attachments, tags, headers)).getOrElse(
      Future.successful(Left(BadRequest))
    )
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
