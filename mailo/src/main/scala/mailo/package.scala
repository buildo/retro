package mailo

import mailo.http.MailClient
import mailo.finder.MailContentFinder
import mailo.parser.HTMLParser

import ingredients.logging._
import nozzle.logging._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext

import scalaz.std.scalaFuture._
import scalaz.{EitherT, \/}

case class MailResponse(
  id: String,
  message: String
)

case class MailRawContent(
  template: String,
  mocks: Map[String, String]
)

object MailRefinedContent {
  sealed abstract class MailRefinedContent(val content: String)

  case class HTMLContent(override val content: String) extends MailRefinedContent(content)
  case class TEXTContent(override val content: String) extends MailRefinedContent(content)
}

abstract class MailoError(message: String) extends RuntimeException(message)

class Mailo(
    mailContentFinder: MailContentFinder,
    mailClient: MailClient,
    loggingLevel: PartialFunction[String, nozzle.logging.EnabledState] = { case "mailo" => Enabled(Level.Debug) }
  )(implicit
    ec: ExecutionContext
  ) {
  import MailRefinedContent._

  private[this] val loggingEnabler: PartialFunction[String, nozzle.logging.EnabledState] = loggingLevel
  implicit val logger = nozzle.logging.BasicLogging(loggingEnabler).logger("mailo")

  def send(
    to: String,
    from: String,
    subject: String,
    templateName: String,
    params: Map[String, String],
    tags: List[String]
  ): Future[\/[MailoError, MailResponse]] = {
    val result = for {
      content <- EitherT(mailContentFinder.find(templateName))
      parsedContent <- EitherT.fromDisjunction(HTMLParser.parse(content, params))
      result <- EitherT(mailClient.send(
        to = to,
        from = from,
        subject = subject,
        content = HTMLContent(parsedContent),
        tags = tags
      ))
    } yield result

    result.run
  }
}
