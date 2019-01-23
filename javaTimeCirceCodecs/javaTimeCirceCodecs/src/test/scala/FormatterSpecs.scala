package io.buildo.tests

import io.buildo.TimeCirceSupport
import java.time._

import io.circe.Json
import io.circe.syntax._
import org.scalatest._

class FormatterSpecs extends FlatSpec with Matchers with TimeCirceSupport {
  val testIstant = Instant.ofEpochMilli(1548265709578l)
  def filterReversedMillis(j : Json) = j.noSpaces.reverse.takeWhile(p => p != '.').filter(_.isDigit)

  "Istant" should "be printed in json with just 3 millis" in {
    filterReversedMillis(testIstant.asJson) should have length 3
  }

  "LocalDateTime" should "be printed in json with just 3 millis" in {
    val date = LocalDateTime.ofInstant(testIstant, ZoneId.systemDefault())
    filterReversedMillis(date.asJson) should have length 3
  }

  "OffsetDateTime" should "be printed in json with just 3 millis" in {
    val offset = OffsetDateTime.ofInstant(testIstant, ZoneId.systemDefault())
    filterReversedMillis(offset.asJson) should have length 3
  }
}
