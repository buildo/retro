package mailo.persistence

import io.circe.{Decoder, Encoder, HCursor, Json}
import akka.http.scaladsl.model.ContentType
import io.circe.DecodingFailure

trait CustomContentTypeCodecs {
  implicit val contentTypeEncoder: Encoder[ContentType] = new Encoder[ContentType] {
    final def apply(c: ContentType): Json =
      Json.obj(("contentType", Json.fromString(c.value)))
  }

  implicit val contentTypeDecoder: Decoder[ContentType] = new Decoder[ContentType] {
    final def apply(c: HCursor): Decoder.Result[ContentType] =
      for {
        contentType <- c.downField("contentType").as[String]
        result <- ContentType
          .parse(contentType)
          .left
          .map(_ => DecodingFailure("Failed decoding content type", Nil))
      } yield result
  }
}
