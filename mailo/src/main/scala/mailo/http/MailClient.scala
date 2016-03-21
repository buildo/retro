package mailo.http

import scalaz.\/
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import mailo.MailoError

object MailClientError {
  case object BadRequest extends MailoError("400 Bad Request - Often missing a required parameter")
  case object Unauthorized extends MailoError("401 Unauthorized - No valid API key provided")
  case object RequestFailed extends MailoError("402 Request Failed - Parameters were valid but request failed")
  case object NotFound extends MailoError("404 Not Found - The requested item doesn’t exist")
  case object ServerError extends MailoError("500, 502, 503, 504 Server Errors - something is wrong on the email server")
  case object UnknownCode extends MailoError("Unknown response")
}

trait MailClient {
  import mailo.MailRefinedContent._
  import mailo.MailResponse
  import MailClientError._

  def send(
    to: String,
    from: String,
    subject: String,
    content: MailRefinedContent,
    tags: List[String]
  )(implicit
    executionContext: ExecutionContext
  ): Future[MailoError \/ MailResponse]
}
