package mailo

import mailo.http.SMTPClient

class MailoSMTPSpec extends munit.FunSuite {
  implicit val ec = munitExecutionContext

  val mailer = Mailo(new MockedData, new SMTPClient, DeliveryGuarantee.AtMostOnce)

  test("should be correctly sent".ignore) {
    mailer
      .send(
        Mail(
          to = "receiver@buildo.io",
          from = "sender@buildo.io",
          subject = "Test mail",
          templateName = "mail.html",
          params = Map.empty[String, String],
          tags = List.empty[String],
        ),
      )
      .map(value => assert(value.isRight))
  }

  test("should correctly fail".ignore) {
    mailer
      .send(
        Mail(
          to = "postmaster@sandbox119020d8ef954c02bac2ee6db24d635b.mailgun.",
          from = "Mailo mailo@buildo.io",
          subject = "Test mail 1",
          templateName = "mail.html",
          params = Map.empty[String, String],
          tags = List.empty[String],
        ),
      )
      .map { value =>
        assertEquals(value, Left(http.MailClientError.BadRequest))
      }
  }

  test("it should be returned".ignore) {
    mailer
      .send(
        Mail(
          to = "mailo@buildo.io",
          from = "Mailo mailo@buildo.io",
          subject = "Test mail 2",
          templateName = "mail.html",
          params = Map("ciao" -> "CIAONI"),
          tags = List("test"),
        ),
      )
      .map { value =>
        assertEquals(value, Left(parser.ParserError.TooManyParamsProvided(Set("ciao"))))
      }
  }
}
