package io.buildo.toctoc
package slick
package authentication

import _root_.slick.jdbc.PostgresProfile.api._

import java.time.Instant
import java.sql.Timestamp

object SlickHelper {
  implicit val boolColumnType = MappedColumnType.base[Instant, Timestamp](
    { i => Timestamp.from(i) },
    { t => t.toInstant() }
  )
}
