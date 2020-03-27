package mailo

import mailo.data.S3MailData
import mailo.http.MailgunClient
import akka.actor.ActorSystem
import akka.testkit.TestKit

class MailoSpec extends munit.FunSuite {
  implicit val ec = munitExecutionContext
  implicit val system = ActorSystem()
  val mailer = Mailo(new S3MailData, new MailgunClient, DeliveryGuarantee.AtMostOnce)

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  test("email should be correctly sent") {
    mailer
      .send(
        Mail(
          to = "mailo@buildo.io",
          from = "Mailo mailo@buildo.io",
          subject = "Test mail",
          templateName = "mail.html",
          params = Map("ciao" -> "CIAOOOONE"),
          tags = List("test"),
        ),
      )
      .map(value => assert(value.isRight))
  }

  test("email with wrong recipient should correctly fail") {
    mailer
      .send(
        Mail(
          to = "postmaster@sandbox119020d8ef954c02bac2ee6db24d635b.mailgun.",
          from = "Mailo mailo@buildo.io",
          subject = "Test mail 1",
          templateName = "mail.html",
          params = Map("ciao" -> "CIAOOOONE"),
          tags = List("test"),
        ),
      )
      .map { value =>
        assertEquals(value, Left(http.MailClientError.BadRequest))
      }

  }

  test("too few parameter error should be returned") {
    mailer
      .send(
        Mail(
          to = "mailo@buildo.io",
          from = "Mailo mailo@buildo.io",
          subject = "Test mail 2",
          templateName = "mail.html",
          params = Map(),
          tags = List("test"),
        ),
      )
      .map { value =>
        assertEquals(value, Left(parser.ParserError.TooFewParamsProvided(Set("ciao"))))
      }
  }

  test("too many parameter error should be returned") {
    mailer
      .send(
        Mail(
          to = "mailo@buildo.io",
          from = "Mailo mailo@buildo.io",
          subject = "Test mail 3",
          templateName = "mail.html",
          params = Map("ciao" -> "CIAONE", "ciaooo" -> "CIAONE"),
          tags = List("test"),
        ),
      )
      .map { value =>
        assertEquals(value, Left(parser.ParserError.TooManyParamsProvided(Set("ciaooo"))))
      }
  }

  test("data error should be returned") {
    mailer
      .send(
        Mail(
          to = "mailo@buildo.io",
          from = "Mailo mailo@buildo.io",
          subject = "Test mail 4",
          templateName = "mail.hl",
          params = Map(),
          tags = List("test"),
        ),
      )
      .map { value =>
        assertEquals(value, Left(data.S3MailDataError.ObjectNotFound))
      }
  }

  test("email should not explode sending attachments") {
    import akka.http.scaladsl.model.MediaTypes._
    import akka.http.scaladsl.model.HttpCharsets._

    val attachment =
      Attachment(name = "test.txt", content = "test", `type` = `text/plain`.withCharset(`UTF-8`))

    mailer
      .send(
        Mail(
          to = "mailo@buildo.io",
          from = "Mailo mailo@buildo.io",
          subject = "Test mail",
          templateName = "mail.html",
          params = Map("ciao" -> "CIAOOOONE"),
          attachments = List(attachment),
          tags = List("test"),
        ),
      )
      .map { value =>
        assert(value.isRight)
      }
  }

  test("email should not explode sending pdf attachments") {
    import akka.http.scaladsl.model.MediaTypes._

    val attachment = Attachment(
      name = "helloworld.pdf",
      content = pdf.get,
      `type` = `application/pdf`,
      transferEncoding = Some("base64"),
    )

    mailer
      .send(
        Mail(
          to = "mailo@buildo.io",
          from = "Mailo mailo@buildo.io",
          subject = "Test mail",
          templateName = "mail.html",
          params = Map("ciao" -> "CIAOOOONE"),
          attachments = List(attachment),
          tags = List("test"),
        ),
      )
      .map { value =>
        assert(value.isRight)
      }
  }
}
