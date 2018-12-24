package mailo

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

import mailo.data.MailData
import mailo.http.{ MailClient, MailgunClient }

import scala.concurrent.{ExecutionContext, Future}

class MockedData extends MailData {
  import scala.concurrent.ExecutionContext.Implicits.global

  override def get(name: String): Future[Either[MailError, MailRawContent]] =
    Future(Right(MailRawContent("ciao", partials = Map("name" -> "claudio"))))
}

//This can be used instead of the MailgunClient to avoid sending emails and annoy Claudio
class MockedClient extends MailClient {
  override def send(
    to: String,
    from: String,
    cc: Option[String],
    bcc: Option[String],
    subject: String,
    content: MailRefinedContent.MailRefinedContent,
    attachments: List[Attachment],
    tags: List[String],
    headers: Map[String, String]
  )(implicit executionContext: ExecutionContext): Future[Either[MailError, MailResponse]] =
    Future(Right(MailResponse("id", "ok")))
}

object AtLeastOnceDryRun extends App {
  import scala.concurrent.ExecutionContext.Implicits.global

  implicit val system = ActorSystem("hellow")
  implicit val materializer = ActorMaterializer()
  val mailClient = new AtLeastOnceMailo(new MockedData, new MailgunClient)
  val fakeMail = Mail(
    to = "mailo@buildo.io",
    from = "Mailo mailo@buildo.io",
    subject = "Hey joe",
    cc = None,
    bcc = None,
    params = Map.empty,
    attachments = Nil,
    templateName = "ciao"
  )

  for {
    _ <- mailClient.send(fakeMail)
    _ <- Future.successful(println("Email sent"))
  } yield ()
}