package io.buildo.toctoc.slick

import java.time.Instant
import java.sql.Timestamp

import slick.jdbc.PostgresProfile.api._
import scala.concurrent.ExecutionContext.Implicits.global

object SlickHelper {
  implicit val boolColumnType = MappedColumnType.base[Instant, Timestamp](
    { i => Timestamp.from(i) },
    { t => t.toInstant() }
  )
}
