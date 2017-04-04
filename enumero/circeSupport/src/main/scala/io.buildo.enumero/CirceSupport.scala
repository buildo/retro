package io.buildo.enumero

import io.circe._

trait CirceSupport {

  private[this] def deserializationError(actual: String, expected: String): String =
    s"Expected a value of type ${expected}, but got ${actual}"

  implicit def encodeCaseEnum[E <: CaseEnum](implicit s: CaseEnumSerialization[E]): Encoder[E] =
    Encoder.encodeString.contramap[E](s.caseToString)

  implicit def decodeCaseEnum[E <: CaseEnum](implicit s: CaseEnumSerialization[E]): Decoder[E] =
    Decoder.decodeString.emap { str =>
      s.caseFromString(str).map(Right(_)).getOrElse(Left(deserializationError(str, s.name)))
    }

}

package object circe extends CirceSupport
