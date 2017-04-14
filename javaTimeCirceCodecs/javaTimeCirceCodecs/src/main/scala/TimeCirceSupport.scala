package io.buildo

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatterBuilder

import io.circe.{ Decoder, Encoder }

import io.circe.java8.time.TimeInstances

trait TimeCirceSupport extends TimeInstances {
  private[this] val dateTimeFormatterBuilder = new DateTimeFormatterBuilder
  //Always have 3 fractional digits
  //.appendInstant converts to a data-time with a zone-offset of UTC formatted as ISO-8601
  private[this] val dateTimeFormatter = dateTimeFormatterBuilder.appendInstant(3).toFormatter

  //overrides default circe encoder/decoder
  implicit val decodeOffsetDateTime: Decoder[OffsetDateTime] = decodeOffsetDateTime(dateTimeFormatter)
  implicit val encodeOffsetDateTime: Encoder[OffsetDateTime] = encodeOffsetDateTime(dateTimeFormatter)
}

package object circeTimeCodecs extends TimeCirceSupport
