package io.buildo.toctoc
package slick
package authentication

import _root_.slick.jdbc.MySQLProfile.api._

import java.time.Instant
import java.sql.Timestamp

object MySqlSlickHelper {
  implicit val timeStampColumnType = MappedColumnType.base[Instant, Timestamp](
    i => Timestamp.from(i),
    t => t.toInstant,
  )
}
