package mailo.persistence

import io.circe.{Decoder, Encoder, HCursor, Json}
import akka.http.scaladsl.model.ContentType
import io.circe.DecodingFailure

trait CustomContentTypeCodecs {
  implicit val encodeFoo: Encoder[ContentType] = new Encoder[ContentType] {
    final def apply(c: ContentType): Json =
      Json.obj(("contentType", Json.fromString(c.value)))
  }

  implicit val decodeFoo: Decoder[ContentType] = new Decoder[ContentType] {
    final def apply(c: HCursor): Decoder.Result[ContentType] =
      for {
        contentType <- c.downField("contentType").as[String]
        result <- ContentType.parse(contentType).left.map(_ => DecodingFailure("Falied decoding content type", Nil))
      } yield result
  }
}
