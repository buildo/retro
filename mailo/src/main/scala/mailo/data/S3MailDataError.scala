package mailo.data

import mailo.MailError

object S3MailDataError {
  case object ObjectNotFound extends MailError("S3 object not found")
  case object BucketNotFound extends MailError("S3 bucket not found")
  case class S3InternalError(override val message: String) extends MailError(message)
}
