package mailo.persistence

import mailo.Mail
import akka.actor.ActorLogging
import akka.persistence._
import io.circe.syntax._
import io.circe.generic.auto._

case class CleanQueue(lastSequenceNr: Long)
case class SendEmail(email: Mail)
case class EmailEvent(content: String)

case class EmailState(events: List[EmailEvent] = Nil) {
  def updated(evt: EmailEvent): EmailState = copy(evt :: events)
  def size: Int = events.length
  override def toString: String = events.reverse.toString
}

class EmailPersistorActor extends PersistentActor
    with ActorLogging with CustomContentTypeCodecs {
  override def persistenceId = "emails-persistence"

  private[this] var state: EmailState = EmailState()

  private[this] def updateState(event: EmailEvent): Unit = state = state.updated(event)

  val receiveRecover: Receive = {
    case SnapshotOffer(_, snapshot: EmailState) => state = snapshot
  }

  private[this] val snapShotInterval = 1000

  val receiveCommand: Receive = {
    case command@CleanQueue(sequenceNr) =>
      log.info("received event {}", command.toString)
      saveSnapshot(state)
      deleteMessages(sequenceNr)
    case command@SendEmail(email) =>
      val data = email.asJson.noSpaces
      log.info("received command {}", command.toString)
      persistAsync(EmailEvent(s"$data")) { event =>
        updateState(event)
        context.system.eventStream.publish(event)
        if (lastSequenceNr % snapShotInterval == 0 && lastSequenceNr != 0)
          saveSnapshot(state)
      }
  }

  val logSnapshotResult: Receive = SnapshotHelper.logSnapshotResult
}
