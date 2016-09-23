package mailo.http

import scalaz.\/
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import mailo.{ MailError, Attachment }

object MailClientError {
  case object BadRequest extends MailError("400 Bad Request - Often missing a required parameter")
  case object Unauthorized extends MailError("401 Unauthorized - No valid API key provided")
  case object RequestFailed extends MailError("402 Request Failed - Parameters were valid but request failed")
  case object NotFound extends MailError("404 Not Found - The requested item doesn’t exist")
  case object ServerError extends MailError("500, 502, 503, 504 Server Errors - something is wrong on the email server")
  case object UnknownCode extends MailError("Unknown response")
  case class UnkownError(msg: String) extends MailError(msg)
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
    attachments: List[Attachment],
    tags: List[String]
  )(implicit
    executionContext: ExecutionContext
  ): Future[MailError \/ MailResponse]
}
