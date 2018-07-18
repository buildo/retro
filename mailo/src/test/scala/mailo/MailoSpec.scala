package mailo

import org.scalatest.{ FlatSpec, Matchers }
import org.scalatest.concurrent.ScalaFutures

import cats.syntax.either._

class MailoSpec extends FlatSpec with AppSpec with Matchers with ScalaFutures {
  import org.scalatest.time.{Span, Seconds, Millis}

  implicit val defaultPatience =
      PatienceConfig(timeout = Span(20, Seconds), interval = Span(5, Seconds))

  "email" should "be correctly sent" in {
    (mailer.send(
      to = "mailo@buildo.io",
      from = "Mailo mailo@buildo.io",
      subject = "Test mail",
      templateName = "mail.html",
      params = Map("ciao" -> "CIAOOOONE"),
      tags = List("test")
    ).futureValue.getOrElse(fail)).message should be ("Queued. Thank you.")
  }

  "email with wrong recipient" should "correctly fail" in {
    mailer.send(
      to = "postmaster@sandbox119020d8ef954c02bac2ee6db24d635b.mailgun.",
      from = "Mailo mailo@buildo.io",
      subject = "Test mail 1",
      templateName = "mail.html",
      params = Map("ciao" -> "CIAOOOONE"),
      tags = List("test")
    ).futureValue.swap.getOrElse(fail) should be (http.MailClientError.BadRequest)
  }

  "too few parameter error" should "be returned" in {
    mailer.send(
      to = "mailo@buildo.io",
      from = "Mailo mailo@buildo.io",
      subject = "Test mail 2",
      templateName = "mail.html",
      params = Map(),
      tags = List("test")
    ).futureValue.swap.getOrElse(fail) should be (parser.ParserError.TooFewParamsProvided(Set("ciao")))
  }

  "too many parameter error" should "be returned" in {
    mailer.send(
      to = "mailo@buildo.io",
      from = "Mailo mailo@buildo.io",
      subject = "Test mail 3",
      templateName = "mail.html",
      params = Map("ciao" -> "CIAONE", "ciaooo" -> "CIAONE"),
      tags = List("test")
    ).futureValue.swap.getOrElse(fail) should be (parser.ParserError.TooManyParamsProvided(Set("ciaooo")))
  }

  "data error" should "be returned" in {
    mailer.send(
      to = "mailo@buildo.io",
      from = "Mailo mailo@buildo.io",
      subject = "Test mail 4",
      templateName = "mail.hl",
      params = Map(),
      tags = List("test")
    ).futureValue.swap.getOrElse(fail) should be (data.S3MailDataError.ObjectNotFound)
  }

  "email" should "not explode sending attachments" in {
    import akka.http.scaladsl.model.MediaTypes._
    import akka.http.scaladsl.model.HttpCharsets._

    val attachment = Attachment(name = "test.txt", content="test", `type`=`text/plain` withCharset `UTF-8`)

    (mailer.send(
       to = "mailo@buildo.io",
       from = "Mailo mailo@buildo.io",
       subject = "Test mail",
       templateName = "mail.html",
       params = Map("ciao" -> "CIAOOOONE"),
       attachments = List(attachment),
       tags = List("test")
     ).futureValue.getOrElse(fail)).message should be ("Queued. Thank you.")
  }

  "email" should "not explode sending pdf attachments" in {
    import akka.http.scaladsl.model.MediaTypes._

    val attachment = Attachment(name = "helloworld.pdf", content = pdf.get, `type` = `application/pdf`, transferEncoding = Some("base64"))

    (mailer.send(
       to = "mailo@buildo.io",
       from = "Mailo mailo@buildo.io",
       subject = "Test mail",
       templateName = "mail.html",
       params = Map("ciao" -> "CIAOOOONE"),
       attachments = List(attachment),
       tags = List("test")
     ).futureValue.getOrElse(fail)).message should be ("Queued. Thank you.")
  }
}

trait AppSpec {
  import akka.stream.ActorMaterializer
  import akka.actor.ActorSystem

  import scala.concurrent.ExecutionContext.Implicits.global

  private[this] implicit val system = ActorSystem()
  private[this] implicit val materializer = ActorMaterializer()

  val mailer = new S3MailgunMailo()
}

