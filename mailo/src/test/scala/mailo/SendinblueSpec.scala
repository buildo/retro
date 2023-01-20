package mailo

import akka.http.scaladsl.model.MediaTypes._
import akka.http.scaladsl.model.HttpCharsets._

import scala.concurrent.ExecutionContext.Implicits.global

class SendinblueSpec extends munit.FunSuite {
  val mailer = new S3SendinblueMailo()

  test("email should be correctly sent") {
    mailer
      .send(
        to = "mailo@buildo.io",
        from = "Mailo test mailo@buildo.io",
        subject = "Test mail",
        templateName = "mail.html",
        params = Map("ciao" -> "CIAOOOONE"),
        tags = List("test"),
      )
      .map { value =>
        assertEquals(value.map(_.message), Right("Email sent successfully."))
      }
  }

  test("email with replyTo should be correctly sent") {
    mailer
      .send(
        to = "mailo@buildo.io",
        from = "Mailo test mailo@buildo.io",
        cc = None,
        bcc = None,
        replyTo = Some("asd@asd.org"),
        subject = "Test mail",
        templateName = "mail.html",
        params = Map("ciao" -> "CIAOOOONE"),
        tags = List("test"),
      )
      .map { value =>
        assertEquals(value.map(_.message), Right("Email sent successfully."))
      }
  }

  test("email with bcc should be correctly sent") {
    mailer
      .send(
        to = "mailo@buildo.io",
        from = "Mailo test mailo@buildo.io",
        cc = None,
        bcc = Some("asd@asd.org"),
        replyTo = None,
        subject = "Test mail",
        templateName = "mail.html",
        params = Map("ciao" -> "CIAOOOONE"),
        tags = List("test"),
      )
      .map { value =>
        assertEquals(value.map(_.message), Right("Email sent successfully."))
      }
  }

  test("email should be correctly sent to multiple recipients") {
    mailer
      .send(
        to = "mailo@buildo.io,email@example.com",
        from = "Mailo test mailo@buildo.io",
        subject = "Test mail",
        templateName = "mail.html",
        params = Map("ciao" -> "CIAOOOONE"),
        tags = List("test"),
      )
      .map { value =>
        assertEquals(value.map(_.message), Right("Email sent successfully."))
      }
  }

  test("email should not be sent if FROM is malformed") {
    mailer
      .send(
        to = "mailo@buildo.io",
        from = "MALFORMED",
        subject = "Test mail",
        templateName = "mail.html",
        params = Map("ciao" -> "CIAOOOONE"),
        tags = List("test"),
      )
      .map { value =>
        assertEquals(
          value,
          Left(
            http.MailClientError.UnknownError(
              "{\"code\":\"invalid_parameter\",\"message\":\"valid sender email required\"}\n",
            ),
          ),
        )
      }
  }

  test("email should not explode sending attachments") {
    val attachment =
      Attachment(name = "test.txt", content = "test", `type` = `text/plain`.withCharset(`UTF-8`))

    mailer
      .send(
        to = "mailo@buildo.io",
        from = "Mailo mailo@buildo.io",
        subject = "Test mail",
        templateName = "mail.html",
        params = Map("ciao" -> "CIAOOOONE"),
        attachments = List(attachment),
        tags = List("test"),
      )
      .map { value =>
        assertEquals(value.map(_.message), Right("Email sent successfully."))
      }
  }

  test("email should not explode sending pdf attachments") {
    val attachment = Attachment(
      name = "helloworld.pdf",
      content = pdf.get,
      `type` = `application/pdf`,
      transferEncoding = Some("base64"),
    )

    mailer
      .send(
        to = "mailo@buildo.io",
        from = "Mailo mailo@buildo.io",
        subject = "Test mail",
        templateName = "mail.html",
        params = Map("ciao" -> "CIAOOOONE"),
        attachments = List(attachment),
        tags = List("test"),
      )
      .map { value =>
        assertEquals(value.map(_.message), Right("Email sent successfully."))
      }
  }
}
