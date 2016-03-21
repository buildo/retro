package mailo

import org.scalatest.{ FlatSpec, Matchers }
import org.scalatest.concurrent.ScalaFutures

class MailoSpec extends FlatSpec with AppSpec with Matchers with ScalaFutures {
  import org.scalatest.time.{Span, Seconds, Millis}

  implicit val defaultPatience =
      PatienceConfig(timeout = Span(10, Seconds), interval = Span(1, Seconds))

  "email" should "be correctly sent" in {
    (mailer.send(
      to = "postmaster@sandbox119020d8ef954c02bac2ee6db24d635b.mailgun.org",
      from = "Mailo postmaster@sandbox119020d8ef954c02bac2ee6db24d635b.mailgun.org",
      subject = "Test mail",
      templateName = "mail.html",
      params = Map("ciao" -> "CIAOOOONE"),
      tags = List("test")
    ).futureValue | (fail)).message should be ("Queued. Thank you.")
  }

  "email with wrong recipient" should "correctly fail" in {
    mailer.send(
      to = "postmaster@sandbox119020d8ef954c02bac2ee6db24d635b.mailgun.",
      from = "Mailo postmaster@sandbox119020d8ef954c02bac2ee6db24d635b.mailgun.org",
      subject = "Test mail 1",
      templateName = "mail.html",
      params = Map("ciao" -> "CIAOOOONE"),
      tags = List("test")
    ).futureValue.swap | (fail) should be (http.MailClientError.BadRequest)
  }

  "too few parameter error" should "be returned" in {
    mailer.send(
      to = "postmaster@sandbox119020d8ef954c02bac2ee6db24d635b.mailgun.org",
      from = "Mailo postmaster@sandbox119020d8ef954c02bac2ee6db24d635b.mailgun.org",
      subject = "Test mail 2",
      templateName = "mail.html",
      params = Map(),
      tags = List("test")
    ).futureValue.swap | (fail) should be (parser.ParserError.TooFewParamsProvided(Set("ciao")))
  }

  "too many parameter error" should "be returned" in {
    mailer.send(
      to = "postmaster@sandbox119020d8ef954c02bac2ee6db24d635b.mailgun.org",
      from = "Mailo postmaster@sandbox119020d8ef954c02bac2ee6db24d635b.mailgun.org",
      subject = "Test mail 3",
      templateName = "mail.html",
      params = Map("ciao" -> "CIAONE", "ciaooo" -> "CIAONE"),
      tags = List("test")
    ).futureValue.swap | (fail) should be (parser.ParserError.TooManyParamsProvided(Set("ciaooo")))
  }

  "finder error" should "be returned" in {
    mailer.send(
      to = "postmaster@sandbox119020d8ef954c02bac2ee6db24d635b.mailgun.org",
      from = "Mailo postmaster@sandbox119020d8ef954c02bac2ee6db24d635b.mailgun.org",
      subject = "Test mail 4",
      templateName = "mail.hl",
      params = Map(),
      tags = List("test")
    ).futureValue.swap | (fail) should be (finder.S3MailContentError.ObjectNotFound)
  }
}

trait AppSpec {
  import akka.stream.ActorMaterializer
  import akka.actor.ActorSystem

  import finder.S3MailContentFinder
  import http.MailgunClient

  import scala.concurrent.ExecutionContext.Implicits.global

  private[this] implicit val system = ActorSystem()
  private[this] implicit val materializer = ActorMaterializer()

  private[this] val s3 = new S3MailContentFinder()
  private[this] val mailgun = new MailgunClient()

  val mailer = new Mailo(s3, mailgun)
}
