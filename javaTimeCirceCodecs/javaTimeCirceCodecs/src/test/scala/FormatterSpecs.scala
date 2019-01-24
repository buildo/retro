package io.buildo.tests

import io.buildo.TimeCirceSupport
import java.time._

import io.circe.Json
import io.circe.syntax._
import org.scalatest._

class FormatterSpecs extends FlatSpec with Matchers with TimeCirceSupport {
  val testInstant = Instant.ofEpochMilli(1548265709578l)
  def filterReversedMillis(j : Json) = j.noSpaces.reverse.takeWhile(p => p != '.').filter(_.isDigit)

  "Instant" should "be printed in json with just 3 millis" in {
    filterReversedMillis(testInstant.asJson) should have length 3
  }

  "LocalDateTime" should "be printed in json with just 3 millis" in {
    val date = LocalDateTime.ofInstant(testInstant, ZoneId.systemDefault())
    filterReversedMillis(date.asJson) should have length 3
  }

  "OffsetDateTime" should "be printed in json with just 3 millis" in {
    val offset = OffsetDateTime.ofInstant(testInstant, ZoneId.systemDefault())
    filterReversedMillis(offset.asJson) should have length 3
  }
}
