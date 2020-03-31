package mailo.persistence

import akka.persistence._

import com.typesafe.scalalogging.LazyLogging

object SnapshotHelper extends LazyLogging {
  val logSnapshotResult: PartialFunction[Any, Unit] = {
    case DeleteMessagesSuccess(toSequenceNr) =>
      logger.debug(s"Successfully deleted messages up to $toSequenceNr")
    case DeleteMessagesFailure(cause, _) =>
      logger.error(s"Unable to delete message because ${cause.getMessage}")
  }
}
