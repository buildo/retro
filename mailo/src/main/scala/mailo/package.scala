package mailo

import java.io.{PrintWriter, StringWriter}

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import cats.data.EitherT
import cats.instances.future._
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.LazyLogging
import mailo.MailRefinedContent._
import mailo.data.{MailData, S3MailData}
import mailo.http.{MailClient, MailgunClient}
import mailo.parser.HTMLParser
import scalacache._
import scalacache.guava._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

class Mailo(
  mailData: MailData,
  mailClient: MailClient
)(
  implicit
  ec: ExecutionContext,
  conf: Config = ConfigFactory.load()
) extends LazyLogging {
  implicit private[this] val scalaCache = ScalaCache(GuavaCache())

  private[this] case class MailoConfig(cachingTTLSeconds: Int)
  private[this] val mailoConfig = MailoConfig(
    cachingTTLSeconds = conf.getInt("mailo.cachingTTLSeconds")
  )

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
    headers: Map[String, String] = Map.empty
  ): Future[Either[MailError, MailResponse]] = {
    val result = for {
      content <- EitherT(cachingWithTTL(templateName)(mailoConfig.cachingTTLSeconds.seconds) {
        mailData.get(templateName)
      })
      _ = logger.debug(s"retrieved $templateName content")
      parsedContent <- EitherT.fromEither(HTMLParser.parse(content, params))
      _ = logger.debug(s"template populated with params: $params")
      result <- EitherT(
        mailClient.send(
          to = to,
          from = from,
          cc = cc,
          bcc = bcc,
          subject = subject,
          content = HTMLContent(parsedContent),
          attachments = attachments,
          tags = tags,
          headers = headers
        )
      )
      _ = logger.info(s"email sent with id: ${result.id}")
    } yield result

    result.value
  }
}

class S3MailgunMailo(
  implicit
  system: ActorSystem,
  materializer: ActorMaterializer,
  ec: ExecutionContext
) {
  private[this] val s3 = new S3MailData()
  private[this] val mailgun = new MailgunClient()

  private[this] val mailgunS3Mailo = new Mailo(s3, mailgun)

  def send(
    to: String,
    from: String,
    cc: Option[String] = None,
    bcc: Option[String] = None,
    subject: String,
    templateName: String,
    params: Map[String, String],
    attachments: List[Attachment] = Nil,
    tags: List[String] = Nil
  ): Future[Either[MailError, MailResponse]] =
    mailgunS3Mailo.send(to, from, cc, bcc, subject, templateName, params, attachments, tags)
}

class S3SendinblueMailo(implicit
  system: ActorSystem,
  materializer: ActorMaterializer,
  ec: ExecutionContext
){
  import data.S3MailData
  import http.SendinblueClient

  private[this] val s3 = new S3MailData()
  private[this] val sendinblue = new SendinblueClient()

  private[this] val sendinblueS3Mailo = new Mailo(s3, sendinblue)

  def send(
    to: String,
    from: String,
    cc: Option[String] = None,
    bcc: Option[String] = None,
    subject: String,
    templateName: String,
    params: Map[String, String],
    attachments: List[Attachment] = Nil,
    tags: List[String] = Nil
  ): Future[Either[MailError, MailResponse]] =
    sendinblueS3Mailo.send(to, from, cc, bcc, subject, templateName, params, attachments, tags)
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
