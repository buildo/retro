package mailo

import java.io.{PrintWriter, StringWriter}

import akka.actor.ActorSystem
import mailo.data.S3MailData
import mailo.http.MailgunClient

import scala.concurrent.{ExecutionContext, Future}

class S3MailgunMailo(
  implicit
  system: ActorSystem,
  ec: ExecutionContext,
) {
  private[this] val s3 = new S3MailData()
  private[this] val mailgun = new MailgunClient()

  private[this] val mailgunS3Mailo = new AtMostOnceMailo(s3, mailgun)

  def send(
    to: String,
    from: String,
    cc: Option[String] = None,
    bcc: Option[String] = None,
    subject: String,
    templateName: String,
    params: Map[String, String],
    attachments: List[Attachment] = Nil,
    tags: List[String] = Nil,
  ): Future[Either[MailError, MailResponse]] =
    mailgunS3Mailo.send(Mail(to, from, cc, bcc, None, subject, templateName, params, attachments, tags))
}

class S3SendinblueMailo(
  implicit ec: ExecutionContext,
) {
  import data.S3MailData
  import http.SendinblueClient

  private[this] val s3 = new S3MailData()
  private[this] val sendinblue = new SendinblueClient()

  private[this] val sendinblueS3Mailo = new AtMostOnceMailo(s3, sendinblue)

  def send(
    to: String,
    from: String,
    cc: Option[String] = None,
    bcc: Option[String] = None,
    replyTo: Option[String] = None,
    subject: String,
    templateName: String,
    params: Map[String, String],
    attachments: List[Attachment] = Nil,
    tags: List[String] = Nil,
  ): Future[Either[MailError, MailResponse]] =
    sendinblueS3Mailo.send(
      Mail(to, from, cc, bcc, replyTo, subject, templateName, params, attachments, tags),
    )
}

package object util {
  implicit class PimpThrowable(t: Throwable) {
    def getStackTraceAsString: String = {
      val sw = new StringWriter
      t.printStackTrace(new PrintWriter(sw))
      sw.toString
    }
  }
}
