package mailo

import java.util.concurrent.ConcurrentLinkedQueue

import mailo.http.MailClient
import mailo.data.MailData
import mailo.persistence.{EmailPersistanceActor, LoggingActor, SendEmail}

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._

import akka.actor.{ActorSystem, Kill}
import akka.pattern.ask
import akka.util.Timeout
import akka.testkit.TestKitBase
import akka.testkit.ImplicitSender
import akka.testkit.TestKit

case class SimpleMail(subject: String)

class MockedClient(val state: ConcurrentLinkedQueue[SimpleMail]) extends MailClient {
  override def send(
    to: String,
    from: String,
    cc: Option[String],
    bcc: Option[String],
    replyTo: Option[String],
    subject: String,
    content: MailRefinedContent.MailRefinedContent,
    attachments: List[Attachment],
    tags: List[String],
    headers: Map[String, String],
  )(implicit executionContext: ExecutionContext): Future[Either[MailError, MailResponse]] =
    Future.successful {
      //State needs to be updated by one at the time
      state.add(SimpleMail(subject))
      Right(MailResponse(subject, "ok"))
    }

  override def sendBatch(
    from: String,
    cc: Option[String],
    bcc: Option[String],
    subject: String,
    content: MailRefinedContent.MailRefinedContent,
    attachments: List[Attachment],
    tags: List[String],
    recipientVariables: Map[String, Map[String, String]],
    headers: Map[String, String],
  )(
    implicit
    executionContext: ExecutionContext,
  ): Future[Either[MailError, MailResponse]] = ???
}

class MockedClientWithDelay(val state: ConcurrentLinkedQueue[SimpleMail]) extends MailClient {
  override def send(
    to: String,
    from: String,
    cc: Option[String],
    bcc: Option[String],
    replyTo: Option[String],
    subject: String,
    content: MailRefinedContent.MailRefinedContent,
    attachments: List[Attachment],
    tags: List[String],
    headers: Map[String, String],
  )(implicit executionContext: ExecutionContext): Future[Either[MailError, MailResponse]] =
    Future.successful {
      //State needs to be updated by one at the time
      Thread.sleep(200)
      state.add(SimpleMail(subject))
      Right(MailResponse(subject, "ok"))
    }

  override def sendBatch(
    from: String,
    cc: Option[String],
    bcc: Option[String],
    subject: String,
    content: MailRefinedContent.MailRefinedContent,
    attachments: List[Attachment],
    tags: List[String],
    recipientVariables: Map[String, Map[String, String]],
    headers: Map[String, String],
  )(
    implicit
    executionContext: ExecutionContext,
  ): Future[Either[MailError, MailResponse]] = ???
}

class MockedData(implicit executionContext: ExecutionContext) extends MailData {
  override def get(name: String): Future[Either[MailError, MailRawContent]] =
    Future(Right(MailRawContent("ciao", partials = Map("name" -> "claudio"))))
}

class PersistenceSpec extends {
  val system: ActorSystem = ActorSystem("testSystem")
} with munit.FunSuite with TestKitBase with ImplicitSender {

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  test("persistence actor should properly deliver email messages") {
    val state = new ConcurrentLinkedQueue[SimpleMail]()
    val emailSender =
      new EmailSender(new MockedData()(munitExecutionContext), new MockedClient(state))(
        munitExecutionContext,
      )
    val loggingActor = system.actorOf(LoggingActor.props())
    val emailPersistanceActor =
      system.actorOf(EmailPersistanceActor.props(emailSender, loggingActor))

    val mail1 = Mail(
      to = "mailo@buildo.io",
      from = "Mailo mailo@buildo.io",
      subject = "1",
      templateName = "mail.html",
      params = Map.empty,
      tags = List("test"),
    )

    val mail2 = mail1.copy(subject = "2")

    emailPersistanceActor ! SendEmail(mail1)
    expectMsg(Queued)

    emailPersistanceActor ! SendEmail(mail2)
    expectMsg(Queued)

    def retry[A](times: Int, interval: Duration)(f: => A)(implicit loc: munit.Location): A = {
      if (times <= 0) fail("Failed after retrying")
      try {
        f
      } catch {
        case _: munit.FailException =>
          Thread.sleep(interval.toMillis)
          retry(times - 1, interval)(f)
      }
    }

    retry(times = 5, interval = 1.second) {
      assertEquals(state.size, 2)
      println(s"${state.size} messages sent")
    }
  }

  test("persistence actor should deliver all the queued messages") {
    val state = new ConcurrentLinkedQueue[SimpleMail]()
    val emailSender =
      new EmailSender(new MockedData()(munitExecutionContext), new MockedClientWithDelay(state))(
        munitExecutionContext,
      )
    val loggingActor = system.actorOf(LoggingActor.props())
    val emailPersistanceActor =
      system.actorOf(EmailPersistanceActor.props(emailSender, loggingActor))

    implicit val enqueueTimeout: Timeout = Timeout(100.milliseconds)

    val mail = Mail(
      to = "mailo@buildo.io",
      from = "Mailo mailo@buildo.io",
      subject = "1",
      templateName = "mail.html",
      params = Map.empty,
      tags = List("test"),
    )

    var queuedEmails = 0
    var failedEmails = 0
    println("start")

    val task = system.scheduler.scheduleAtFixedRate(0.seconds, 1.milliseconds)(
      () =>
        ask(emailPersistanceActor, SendEmail(mail)).onComplete {
          case scala.util.Success(_) => queuedEmails += 1
          case scala.util.Failure(_) => failedEmails += 1
        }(munitExecutionContext),
    )(munitExecutionContext)

    Thread.sleep(5000)

    emailPersistanceActor ! Kill

    Thread.sleep(200)
    task.cancel()

    Thread.sleep(100)
    val receivedEmails = state.size

    assert(queuedEmails <= receivedEmails)
    println(
      s"queued $queuedEmails emails, received ${receivedEmails}, failed (not queued) $failedEmails",
    )
  }
}
