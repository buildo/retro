package io.buildo

import java.time.{Instant, OffsetDateTime}
import java.time.format.DateTimeFormatterBuilder

import io.circe.java8.time.JavaTimeEncoders
import io.circe.{Encoder, Json}

trait TimeCirceSupport extends JavaTimeEncoders{
  private[this] val dateTimeFormatterBuilder = new DateTimeFormatterBuilder
  //Always have 3 fractional digits
  //.appendInstant converts to a data-time with a zone-offset of UTC formatted as ISO-8601
  private[this] val dateTimeFormatter = dateTimeFormatterBuilder.appendInstant(3).toFormatter

  //overrides default circe encoder/decoder
  implicit val encodeOffsetDateTimeWIth3Millis: Encoder[OffsetDateTime] = encodeOffsetDateTimeWithFormatter(dateTimeFormatter)
  implicit val encodeInstantWithMillis: Encoder[Instant] = ( i: Instant ) => Json.fromString(dateTimeFormatter.format(i))
}

package object circeTimeCodecs extends TimeCirceSupport
