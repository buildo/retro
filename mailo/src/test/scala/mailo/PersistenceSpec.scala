package mailo

import akka.actor.{ActorSystem, PoisonPill}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import akka.testkit.{TestKit, TestProbe, ImplicitSender}

import cats.syntax.either._

import mailo.http.MailClient
import mailo.data.MailData
import mailo.persistence.{EmailPersistanceActor, SendEmail, LoggingActor}

import org.scalatest.{Matchers, BeforeAndAfterAll, Suite, WordSpecLike, FeatureSpec}
import org.scalatest.concurrent.{ScalaFutures, Eventually}
import org.scalatest.time.{Span, Seconds}

import scala.collection.mutable.Queue
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._

case class SimpleMail(subject: String)
class MockedClient(val state: Queue[SimpleMail]) extends MailClient {
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
  )(implicit executionContext: ExecutionContext): Future[Either[MailError, MailResponse]] = Future.successful {
    //State needs to be updated by one at the time
    synchronized {
      state.enqueue(SimpleMail(subject))
      Right(MailResponse(subject, "ok"))
    }
  }
}

class MockedData extends MailData {
  import scala.concurrent.ExecutionContext.Implicits.global

  override def get(name: String): Future[Either[MailError, MailRawContent]] =
    Future(Right(MailRawContent("ciao", partials = Map("name" -> "claudio"))))
}

class PersistenceSpec
  extends TestKit(ActorSystem("testSystem"))
  with ImplicitSender
  with WordSpecLike
  with Matchers
  with BeforeAndAfterAll
  with ScalaFutures {
  import Eventually._

  implicit def executionContext: ExecutionContext = system.dispatcher
 
  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  implicit val defaultPatience =
      PatienceConfig(timeout = Span(20, Seconds), interval = Span(5, Seconds))

  "persistence actor" should {
    "properly deliver email messages" in {
      val state = new Queue[SimpleMail]()
      val emailSender = new EmailSender(new MockedData, new MockedClient(state))
      val loggingActor = system.actorOf(LoggingActor.props())
      val emailPersistanceActor = system.actorOf(EmailPersistanceActor.props(emailSender, loggingActor))

      val mail1 = Mail(
        to = "mailo@buildo.io",
        from = "Mailo mailo@buildo.io",
        subject = "1",
        templateName = "mail.html",
        params = Map.empty,
        tags = List("test")
      )

      val mail2 = mail1.copy(subject = "2")

      emailPersistanceActor ! SendEmail(mail1)
      expectMsg(Queued)

      emailPersistanceActor ! SendEmail(mail2)
      expectMsg(Queued)

      eventually(timeout(Span(5, Seconds))) {
        state.size should be(2)
        info(s"${state.size} messages sent")
      }
    }
  }
}