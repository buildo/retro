package mailo.persistence

import scala.concurrent.Future
import scala.util.{Success, Failure}

import mailo.Mail
import akka.actor.{Actor, ActorLogging, Props, Status, ActorRef}
import akka.persistence._
import io.circe.syntax._
import io.circe.generic.auto._
import io.circe.parser._
import mailo.data.MailData
import mailo.http.MailClient

case object Ack
case class SendEmail(email: Mail)
case class EmailEvent(content: String)
case class EmailApplicativeErrorEvent(emailEvent: EmailEvent, errorMessage: String)
case class EmailCommunicationErrorEvent(emailCommand: SendEmail, errorMessage: String)

object EmailPersistanceActor {
  def props(emailSender: mailo.Mailo) =
    Props(new EmailPersistanceActor(emailSender))
}

object DeadEmailsHandlerActor {
  def props(senderActor: ActorRef) =
    Props(new DeadEmailsHandlerActor(senderActor))
}

class EmailPersistanceActor(emailSender: mailo.Mailo) extends PersistentActor
    with ActorLogging with CustomContentTypeCodecs with AtLeastOnceDelivery {
  import context.dispatcher

  override def persistenceId = "emails-persistence"
  val eventStream = context.system.eventStream

  def send(email: Mail) = emailSender.send(email)

  val receiveRecover: Receive = {
    case e@EmailEvent(json) =>
      decode[Mail](json) match {
        case Right(email) => send(email)
        case Left(error) => eventStream.publish(EmailApplicativeErrorEvent(e, error.getMessage))
      }
  }

  val receiveCommand: Receive = {
    case command@SendEmail(email) =>
      log.info("received command {}", command.toString)
      persist(EmailEvent(email.asJson.noSpaces)) { event =>
        sender() ! Ack
        send(email).onComplete {
          case Success(result) =>
            result match {
              case Right(_) => ()
                context.system.eventStream.publish(event)
              case Left(error) =>
                eventStream.publish(EmailApplicativeErrorEvent(event, error.getMessage))
            }
            deleteMessages(lastSequenceNr)
          case Failure(reason) =>
            eventStream.publish(EmailCommunicationErrorEvent(command, reason.getMessage))
            deleteMessages(lastSequenceNr)
        }
      }
  }

  val logSnapshotResult: Receive = SnapshotHelper.logSnapshotResult
}

class DeadEmailsHandlerActor(senderActor: ActorRef) extends Actor with ActorLogging with CustomContentTypeCodecs {
  val eventStream = context.system.eventStream
  override def preStart =
    eventStream.subscribe(self, classOf[EmailApplicativeErrorEvent]) &&
    eventStream.subscribe(self, classOf[EmailCommunicationErrorEvent])

  def receive = {
    case EmailApplicativeErrorEvent(event, errorMessage) =>
      log.error(s"%{event} failed with error: ${errorMessage}")
    case EmailCommunicationErrorEvent(sendEmail, errorMessage) =>
      log.error(s"${sendEmail} failed with error ${errorMessage}, rescheduling the message")
      senderActor ! sendEmail
  }
}