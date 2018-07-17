package mailo
package http

import com.typesafe.scalalogging.LazyLogging

import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.{Authorization, BasicHttpCredentials, RawHeader}
import akka.http.scaladsl.marshalling._
import akka.http.scaladsl.unmarshalling._

import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import akka.actor.ActorSystem

import scala.concurrent.{Future, ExecutionContext}
import javax.mail.Message.RecipientType
import javax.mail.internet.MimeMessage

import com.typesafe.config.{Config, ConfigFactory}

import cats.data.EitherT
import cats.syntax.either._
import cats.instances.future._
import cats.instances.either._

import akka.http.scaladsl.model.ContentType
import akka.util.ByteString

import scala.util.Try
import util._
import com.sendinblue.Sendinblue

class SendinblueClient(
  implicit
  system: ActorSystem,
  materializer: ActorMaterializer,
  conf: Config = ConfigFactory.load()
) extends MailClient
    with MimeMailClient
    with LazyLogging {
  import MailClientError._
  import mailo.MailRefinedContent._
  import mailo.MailResponse
  import mailo.MailError

  private[this] case class SendinblueConfig(key: String)
  private[this] val sendinblueConfig = SendinblueConfig(
    key = conf.getString("mailo.sendinblue.key")
  )
  private[this] val sendinblue = new Sendinblue("https://api.sendinblue.com/v2.0", sendinblueConfig.key)


  def sendMime(
    message: MimeMessage,
    tags: List[String] = List.empty,
    attachments: List[Attachment] = List.empty,
    headers: Map[String, String] = Map.empty
  )(
    implicit
    executionContext: ExecutionContext
  ): Future[Either[MailError, MailResponse]] =
    throw new Exception("unable to send mime messages in Sendinblue")


  def send(
    to: String,
    from: String,
    cc: Option[String] = None,
    bcc: Option[String] = None,
    subject: String,
    content: MailRefinedContent,
    attachments: List[Attachment],
    tags: List[String],
    headers: Map[String, String] = Map.empty
  )(
    implicit
    executionContext: scala.concurrent.ExecutionContext
  ): Future[Either[MailError, MailResponse]] =
    for {
      entity <- entity(
        from = from,
        to = to,
        cc = cc,
        bcc = bcc,
        subject = subject,
        content = content,
        attachments = attachments,
        tags = tags,
        headers = headers
      )
      res <- Future(sendinblue.send_email(entity))
    } yield {
      println(res)
      res match {
        case _ => MailResponse("", res).asRight[MailError]
      }
    }

  private[this] def entity(
    from: String,
    to: String,
    cc: Option[String],
    bcc: Option[String],
    subject: String,
    content: MailRefinedContent,
    attachments: List[Attachment],
    tags: List[String],
    headers: Map[String, String]
  )(implicit ec: ExecutionContext): Future[java.util.HashMap[String, Any]] = Future {
    import mailo.MailRefinedContent._

    val data = new java.util.HashMap[String, Any]()

    val toMap = new java.util.HashMap[String, String]()
    toMap.put(to, "")
    data.put("to", toMap)

    data.put("from", Array(from))

    data.put("subject", subject)

    val headersMap = new java.util.HashMap[String, String]()
    if (tags.size > 0)
      headersMap.put("X-Mailin-Tag", tags.mkString(", "))
    headers.keys.foreach { k =>
      headersMap.put(k, headers(k))
    }
    data.put("headers", headers)


    val attachmentsMap = new java.util.HashMap[String, String]()
    attachments.foreach { a =>
      attachmentsMap.put(a.name, a.content)
    }
    data.put("attachment", attachmentsMap)

    content match {
      case HTMLContent(html) => data.put("html", html)
      case TEXTContent(text) => data.put("html", ""); data.put("text", text)
    }

    data
  }


}
