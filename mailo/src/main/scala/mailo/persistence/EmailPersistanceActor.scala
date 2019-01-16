package mailo.persistence

import scala.concurrent.Future
import scala.util.{Success, Failure}

import mailo.Mail
import akka.actor.{Actor, ActorLogging, Props, Status}
import akka.persistence._
import io.circe.syntax._
import io.circe.generic.auto._
import io.circe.parser._
import mailo.data.MailData
import mailo.http.MailClient

case object Ack
case class SendEmail(email: Mail)
case class EmailEvent(content: String)
case class EmailErrorEvent(emailEvent: EmailEvent, errorMessage: String)

object EmailPersistanceActor {
  def props(emailSender: mailo.Mailo) =
    Props(new EmailPersistanceActor(emailSender))
}

object DeadEmailsHandlerActor {
  def props() =
    Props(new DeadEmailsHandlerActor())
}

class EmailPersistanceActor(emailSender: mailo.Mailo) extends PersistentActor
    with ActorLogging with CustomContentTypeCodecs with AtLeastOnceDelivery {
  import context.dispatcher

  override def persistenceId = "emails-persistence"

  def send(email: Mail) = emailSender.send(email)

  val receiveRecover: Receive = {
    case EmailEvent(json) =>
      //TODO this should be sent to a dead letters queue
      val email = decode[Mail](json).getOrElse(throw new Exception("Cannot decode the persisted email... this is very wrong"))
      send(email)
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
                context.system.eventStream.publish(EmailErrorEvent(event, error.getMessage))
            }
            deleteMessages(lastSequenceNr)
          case Failure(reason) =>
            //This is a communication error, do not delete messages, retry next time
            log.error(reason.getMessage)
        }
      }
  }

  val logSnapshotResult: Receive = SnapshotHelper.logSnapshotResult
}

class DeadEmailsHandlerActor() extends Actor with ActorLogging with CustomContentTypeCodecs {
  override def preStart = context.system.eventStream.subscribe(self, classOf[EmailErrorEvent])

  def receive = {
    case EmailErrorEvent(content, errorMessage) =>
      log.error(s"%{content} failed with error: ${errorMessage}")
  }
}