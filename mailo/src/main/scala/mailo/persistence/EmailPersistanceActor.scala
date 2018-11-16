package mailo.persistence

import mailo.Mail
import akka.actor.ActorLogging
import akka.persistence._
import io.circe.syntax._
import io.circe.generic.auto._

case object CleanQueue
case class SendEmail(email: Mail)
case class EmailEvent(content: String)

class EmailPersistanceActor extends PersistentActor
    with ActorLogging with CustomContentTypeCodecs {
  override def persistenceId = "emails-persistence"

  def send(content: String): Unit = println(content)

  val receiveRecover: Receive = {
    case EmailEvent(content) => send(content)
  }

  val receiveCommand: Receive = {
    case command@CleanQueue =>
      log.info("received event {}", command.toString)
      deleteMessages(lastSequenceNr)
    case command@SendEmail(email) =>
      log.info("received command {}", command.toString)
      persist(EmailEvent(email.asJson.noSpaces)) { event =>
        send(event.content)
        context.system.eventStream.publish(event)
      }
  }

  val logSnapshotResult: Receive = SnapshotHelper.logSnapshotResult
}
