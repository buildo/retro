package io.buildo.tests

import io.buildo.TimeCirceSupport
import java.time._

import io.circe.Json
import io.circe.syntax._

class FormatterSpecs extends munit.FunSuite with TimeCirceSupport {
  val testInstant = Instant.ofEpochMilli(1548265709578L)
  def filterReversedMillis(j: Json) = j.noSpaces.reverse.takeWhile(p => p != '.').filter(_.isDigit)

  test("Instant should be printed in json with just 3 millis") {
    assertEquals(filterReversedMillis(testInstant.asJson).length, 3)
  }

  test("LocalDateTime should be printed in json with just 3 millis") {
    val date = LocalDateTime.ofInstant(testInstant, ZoneId.systemDefault())
    assertEquals(filterReversedMillis(date.asJson).length, 3)
  }

  test("OffsetDateTime should be printed in json with just 3 millis") {
    val offset = OffsetDateTime.ofInstant(testInstant, ZoneId.systemDefault())
    assertEquals(filterReversedMillis(offset.asJson).length, 3)
  }
}
