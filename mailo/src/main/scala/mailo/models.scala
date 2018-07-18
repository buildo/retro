package mailo

import akka.http.scaladsl.model.ContentType

import io.buildo.enumero.annotations.enum

@enum trait DeliverySemantic {
  object AtMostOnce
  object AtLeastOnce
}

case class Attachment(
  name: String,
  `type`: ContentType,
  content: String,
  transferEncoding: Option[String] = None
)

sealed trait MailResult
case class MailResponse(
  id: String,
  message: String
) extends MailResult
case object LocallyQueued extends MailResult

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

case class Mail(
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
)
