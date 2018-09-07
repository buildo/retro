package mailo

import cats.data.EitherT
import cats.instances.future._
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.LazyLogging
import mailo.MailRefinedContent._
import mailo.data.MailData
import mailo.http.MailClient
import mailo.parser.HTMLParser
import scalacache._
import scalacache.guava._

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext

class AtMostOnceMailo(
  val mailData: MailData,
  val mailClient: MailClient,
)(
  implicit
  ec: ExecutionContext,
  conf: Config = ConfigFactory.load()
) extends Mailo with LazyLogging {
  implicit private[this] val scalaCache = ScalaCache(GuavaCache())

  private[this] case class MailoConfig(cachingTTLSeconds: Int)
  private[this] val mailoConfig = MailoConfig(
    cachingTTLSeconds = conf.getInt("mailo.cachingTTLSeconds")
  )

  def send(mail: Mail) = {
    val result = for {
      content <- EitherT(cachingWithTTL(mail.templateName)(mailoConfig.cachingTTLSeconds.seconds) {
        mailData.get(mail.templateName)
      })
      _ = logger.debug(s"retrieved ${mail.templateName} content")
      parsedContent <- EitherT.fromEither(HTMLParser.parse(content, mail.params))
      _ = logger.debug(s"template populated with params: ${mail.params}")
      result <- EitherT(
        mailClient.send(
          to = mail.to,
          from = mail.from,
          cc = mail.cc,
          bcc = mail.bcc,
          subject = mail.subject,
          content = HTMLContent(parsedContent),
          attachments = mail.attachments,
          tags = mail.tags,
          headers = mail.headers
        )
      )
      _ = logger.info(s"email sent with id: ${result.id}")
    } yield result

    result.value
  }
}
