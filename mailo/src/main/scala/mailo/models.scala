package mailo

import akka.http.scaladsl.model.ContentType

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
