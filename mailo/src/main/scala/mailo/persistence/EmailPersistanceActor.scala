package mailo.persistence

import scala.util.{Failure, Success}

import mailo.Mail
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Props
import akka.persistence._
import io.circe.syntax._
import io.circe.generic.auto._
import io.circe.parser._

case class SendEmail(email: Mail)
case class EmailEvent(content: String)
case class EmailApplicativeErrorEvent(emailEvent: EmailEvent, errorMessage: String)
case class EmailCommunicationErrorEvent(emailCommand: SendEmail, errorMessage: String)

object EmailPersistanceActor {
  def props(emailSender: mailo.Mailo, deadLettersHandler: ActorRef) =
    Props(new EmailPersistanceActor(emailSender, deadLettersHandler: ActorRef))
}

object LoggingActor {
  def props() =
    Props(new LoggingActor())
}

class EmailPersistanceActor(
  emailSender: mailo.Mailo,
  deadLettersHandler: ActorRef,
) extends PersistentActor
    with ActorLogging
    with CustomContentTypeCodecs
    with AtLeastOnceDelivery {
  import context.dispatcher

  override def persistenceId = "emails-persistence"
  val eventStream = context.system.eventStream

  def send(email: Mail) = emailSender.send(email)

  val receiveRecover: Receive = { case e @ EmailEvent(json) =>
    decode[Mail](json) match {
      case Right(email) =>
        send(email)
        ()
      case Left(error) => deadLettersHandler ! EmailApplicativeErrorEvent(e, error.getMessage)
    }
  }

  val receiveCommand: Receive = { case command @ SendEmail(email) =>
    log.info("received command {}", command.toString)
    persist(EmailEvent(email.asJson.noSpaces)) { event =>
      sender() ! mailo.Queued
      send(email).onComplete {
        case Success(result) =>
          result match {
            case Right(_) =>
              ()
              eventStream.publish(event)
            case Left(error) =>
              deadLettersHandler ! EmailApplicativeErrorEvent(event, error.getMessage)
          }
          deleteMessages(lastSequenceNr)
        case Failure(reason) =>
          deadLettersHandler ! EmailCommunicationErrorEvent(command, reason.getMessage)
          deleteMessages(lastSequenceNr)
      }
    }
  }

  val logSnapshotResult: Receive = SnapshotHelper.logSnapshotResult
}

class LoggingActor() extends Actor with ActorLogging with CustomContentTypeCodecs {
  def receive = {
    case EmailApplicativeErrorEvent(_, errorMessage) =>
      log.error(s"%{event} failed with error: ${errorMessage}")
    case EmailCommunicationErrorEvent(sendEmail, errorMessage) =>
      log.error(s"${sendEmail} failed with error ${errorMessage},")
  }
}
