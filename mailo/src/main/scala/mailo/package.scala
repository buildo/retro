package mailo

import mailo.http.MailClient
import mailo.data.MailData
import mailo.parser.HTMLParser

import scala.concurrent.Future
import scala.concurrent.ExecutionContext

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

import scalaz.std.scalaFuture._
import scalaz.{EitherT, \/}

import com.typesafe.config.{ ConfigFactory, Config }

import akka.http.scaladsl.model.ContentType

import scala.concurrent.duration._
import scalacache._
import guava._

import java.io.{ StringWriter, PrintWriter }

case class Attachment(
  name: String,
  `type`: ContentType,
  content: String,
  transferEncoding: Option[String] = None
)

case class MailResponse(
  id: String,
  message: String
)

case class MailRawContent(
  template: String,
  partials: Map[String, String]
)

object MailRefinedContent {
  sealed abstract class MailRefinedContent(val content: String)

  case class HTMLContent(override val content: String) extends MailRefinedContent(content)
  case class TEXTContent(override val content: String) extends MailRefinedContent(content)
}

abstract class MailError(message: String) extends RuntimeException(message)

class Mailo(
    mailData: MailData,
    mailClient: MailClient
  )(implicit
    ec: ExecutionContext,
    conf: Config = ConfigFactory.load()
  ) {
  import MailRefinedContent._

  implicit private[this] val scalaCache = ScalaCache(GuavaCache())

  private[this] case class MailoConfig(cachingTTLSeconds: Int)
  private[this] val mailoConfig = MailoConfig(
    cachingTTLSeconds = conf.getInt("mailo.cachingTTLSeconds")
  )

  def send(
    to: String,
    from: String,
    subject: String,
    templateName: String,
    params: Map[String, String],
    attachments: List[Attachment] = Nil,
    tags: List[String] = Nil
  ): Future[\/[MailError, MailResponse]] = {
    val result = for {
      content <- EitherT(cachingWithTTL(templateName)(mailoConfig.cachingTTLSeconds.seconds) {
        mailData.get(templateName)
      })
      parsedContent <- EitherT.fromDisjunction(HTMLParser.parse(content, params))
      result <- EitherT(mailClient.send(
        to = to,
        from = from,
        subject = subject,
        content = HTMLContent(parsedContent),
        attachments = attachments,
        tags = tags
      ))
    } yield result

    result.run
  }
}

class S3MailgunMailo(implicit
  system: ActorSystem,
  materializer: ActorMaterializer,
  ec: ExecutionContext
){
  import data.S3MailData
  import http.MailgunClient

  private[this] val s3 = new S3MailData()
  private[this] val mailgun = new MailgunClient()

  private[this] val mailgunS3Mailo = new Mailo(s3, mailgun)

  def send(
    to: String,
    from: String,
    subject: String,
    templateName: String,
    params: Map[String, String],
    attachments: List[Attachment] = Nil,
    tags: List[String] = Nil
  ): Future[\/[MailError, MailResponse]] =
    mailgunS3Mailo.send(to, from, subject, templateName, params, attachments, tags)
}

package object util {
  implicit class PimpThrowable(t: Throwable) {
    def getStackTraceAsString = {
      val sw = new StringWriter
      t.printStackTrace(new PrintWriter(sw))
      sw.toString
    }
  }
}
