package io.buildo.enumero

import io.circe.{KeyDecoder, KeyEncoder}
import io.circe._

trait CirceSupport {

  private[this] def deserializationError(actual: String, expected: String): String =
    s"Expected a value of type ${expected}, but got ${actual}"

  private[this] def deserializationErrorForIndex(actual: String, expected: String): String =
    s"Expected an indexed value for type ${expected}, but got ${actual}"

  implicit def encodeCaseEnum[E <: CaseEnum](implicit s: CaseEnumSerialization[E]): Encoder[E] =
    Encoder.encodeString.contramap[E](s.caseToString)

  implicit def decodeCaseEnum[E <: CaseEnum](implicit s: CaseEnumSerialization[E]): Decoder[E] =
    Decoder.decodeString.emap { str =>
      s.caseFromString(str)
        .map(Right(_))
        .getOrElse(Left(deserializationError(str, s.name)))
    }

  implicit final def decodeKeyCaseEnum[E <: CaseEnum](
    implicit s: CaseEnumSerialization[E]
  ): KeyDecoder[E] =
    KeyDecoder.instance[E](s.caseFromString)

  implicit final def encodeKeyCaseEnum[E <: CaseEnum](
    implicit s: CaseEnumSerialization[E]
  ): KeyEncoder[E] =
    KeyEncoder.instance[E](s.caseToString)

  implicit def encodeIndexedCaseEnum[E <: IndexedCaseEnum](
    implicit c: CaseEnumIndex[E],
    e: Encoder[E#Index]
  ): Encoder[E] = e.contramap(c.caseToIndex)

  implicit def decodeIndexedCaseEnum[E <: IndexedCaseEnum](
    implicit c: CaseEnumIndex[E],
    s: CaseEnumSerialization[E],
    d: Decoder[E#Index]
  ): Decoder[E] = d.emap { index =>
    c.caseFromIndex(index)
      .map(Right(_))
      .getOrElse(
        Left(deserializationErrorForIndex(index.toString, s.name))
      )
  }
}

package object circe extends CirceSupport
