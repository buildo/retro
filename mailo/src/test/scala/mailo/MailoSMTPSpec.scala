package mailo

import org.scalatest.{FlatSpec, Matchers}
import org.scalatest.concurrent.ScalaFutures
import cats.syntax.either._
import mailo.data.S3MailData
import mailo.http.{MailgunClient, SMTPClient}

class MailoSMTPSpec extends FlatSpec with AppSpecSmtpClient with Matchers with ScalaFutures {
  import org.scalatest.time.{Span, Seconds}

  implicit val defaultPatience =
      PatienceConfig(timeout = Span(20, Seconds), interval = Span(5, Seconds))

  "email" should "be correctly sent" in {
    val a = mailer.send(Mail(
      to = "receiver@buildo.io",
      from = "sender@buildo.io",
      subject = "Test mail",
      templateName = "mail.html",
      params = Map.empty[String, String],
      tags = List.empty[String]
    )).futureValue.isRight should be (true)
  }

  "email with wrong recipient" should "correctly fail" in {
    mailer.send(Mail(
      to = "postmaster@sandbox119020d8ef954c02bac2ee6db24d635b.mailgun.",
      from = "Mailo mailo@buildo.io",
      subject = "Test mail 1",
      templateName = "mail.html",
      params = Map.empty[String, String],
      tags = List.empty[String]
    )).futureValue.swap.getOrElse(fail) should be (http.MailClientError.BadRequest)
  }

  "too few parameter error" should "be returned" in {
    mailer.send(Mail(
      to = "mailo@buildo.io",
      from = "Mailo mailo@buildo.io",
      subject = "Test mail 2",
      templateName = "mail.html",
      params = Map("ciao" -> "CIAONI"),
      tags = List("test")
    )).futureValue.swap.getOrElse(fail) should be (parser.ParserError.TooManyParamsProvided(Set("ciao")))
  }
}

trait AppSpecSmtpClient {
  import akka.stream.ActorMaterializer
  import akka.actor.ActorSystem

  import scala.concurrent.ExecutionContext.Implicits.global

  private[this] implicit val system = ActorSystem()
  private[this] implicit val materializer = ActorMaterializer()

  val mailer = Mailo(new MockedData, new SMTPClient, DeliveryGuarantee.AtMostOnce)
}

