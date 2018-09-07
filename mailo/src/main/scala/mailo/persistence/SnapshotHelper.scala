package mailo.persistence

import akka.persistence._

import com.typesafe.scalalogging.LazyLogging

object SnapshotHelper extends LazyLogging {
  val logSnapshotResult: PartialFunction[Any, Unit] = {
    case SaveSnapshotSuccess(metadata) =>
      logger.debug(s"Snaposhot successfully stored")
    case SaveSnapshotFailure(metadata, reason) =>
      logger.error(s"Unable to save snapshot because $reason")
    case DeleteMessagesSuccess(toSequenceNr) =>
      logger.debug(s"Successfully deleted messages up to $toSequenceNr")
    case DeleteMessagesFailure(cause, toSequenceNr) =>
      logger.error(s"Unable to delete message because ${cause.getMessage}")
  }
}
