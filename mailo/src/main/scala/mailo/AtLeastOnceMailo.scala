package mailo

import akka.actor.ActorSystem
import akka.util.Timeout
import akka.pattern.ask
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.LazyLogging
import mailo.data.MailData
import mailo.http.MailClient
import mailo.persistence.{EmailPersistanceActor, LoggingActor, SendEmail}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

case class MailPersistenceError(override val message: String) extends MailError(message)

class AtLeastOnceMailo(
  val data: MailData,
  val client: MailClient,
)(
  implicit
  ec: ExecutionContext,
  conf: Config = ConfigFactory.load(),
  system: ActorSystem = ActorSystem("mailo"),
  enqueueTimeout: Timeout = Timeout(200 milliseconds),
) extends Mailo
    with LazyLogging {
  private[this] val emailSender = new EmailSender(data, client)
  private[this] val loggingActor = system.actorOf(LoggingActor.props())
  private[this] val emailPersistanceActor =
    system.actorOf(EmailPersistanceActor.props(emailSender, loggingActor))

  def send(mail: Mail): Future[Either[MailError, MailResult]] = {
    ask(emailPersistanceActor, SendEmail(mail))
      .mapTo[MailResult]
      .map(Right(_))
  }
}
