package mailo

import akka.stream.ActorMaterializer
import akka.actor.ActorSystem

import akka.http.scaladsl.model.MediaTypes._
import akka.http.scaladsl.model.HttpCharsets._

import cats.syntax.either._

import org.scalatest.{FlatSpec, Matchers}
import org.scalatest.concurrent.ScalaFutures

import org.scalatest.time.{Seconds, Span}

import scala.concurrent.ExecutionContext.Implicits.global

class SendinblueSpec extends FlatSpec with Matchers with ScalaFutures {
  private[this] implicit val system = ActorSystem()
  private[this] implicit val materializer = ActorMaterializer()

  val mailer = new S3SendinblueMailo()

  implicit val defaultPatience =
    PatienceConfig(timeout = Span(20, Seconds), interval = Span(5, Seconds))

  "email" should "be correctly sent" in {
    (mailer
      .send(
        to = "mailo@buildo.io",
        from = "Mailo test mailo@buildo.io",
        subject = "Test mail",
        templateName = "mail.html",
        params = Map("ciao" -> "CIAOOOONE"),
        tags = List("test")
      )
      .futureValue
      .getOrElse(fail))
      .message should be("Email sent successfully.")
  }

  "email" should "not be sent if FROM is malformed" in {
    (mailer
      .send(
        to = "mailo@buildo.io",
        from = "MALFORMED",
        subject = "Test mail",
        templateName = "mail.html",
        params = Map("ciao" -> "CIAOOOONE"),
        tags = List("test")
      )
      .futureValue
      .swap
      .getOrElse(fail)) should be (http.MailClientError.UnknownError("{\"code\":\"missing_parameter\",\"message\":\"sender name is missing\"}"))
  }

 "email" should "not explode sending attachments" in {
    val attachment =
      Attachment(name = "test.txt", content = "test", `type` = `text/plain`.withCharset(`UTF-8`))

    (mailer
      .send(
        to = "mailo@buildo.io",
        from = "Mailo mailo@buildo.io",
        subject = "Test mail",
        templateName = "mail.html",
        params = Map("ciao" -> "CIAOOOONE"),
        attachments = List(attachment),
        tags = List("test")
      )
      .futureValue
      .getOrElse(fail))
      .message should be("Email sent successfully.")
  }

  "email" should "not explode sending pdf attachments" in {
    val attachment = Attachment(
      name = "helloworld.pdf",
      content = pdf.get,
      `type` = `application/pdf`,
      transferEncoding = Some("base64")
    )

    (mailer
      .send(
        to = "mailo@buildo.io",
        from = "Mailo mailo@buildo.io",
        subject = "Test mail",
        templateName = "mail.html",
        params = Map("ciao" -> "CIAOOOONE"),
        attachments = List(attachment),
        tags = List("test")
      )
      .futureValue
      .getOrElse(fail))
      .message should be("Email sent successfully.")
  }
}
