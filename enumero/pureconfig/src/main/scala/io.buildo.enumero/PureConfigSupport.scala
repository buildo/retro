package io.buildo.enumero

import pureconfig._
import pureconfig.generic.auto._

trait PureConfigSupport {
  implicit def decodeCaseEnum[T <: CaseEnum](implicit s: CaseEnumSerialization[T]) =
    ConfigReader[String].map(x => s.caseFromString(x).get)

  implicit def decodeIndexedCaseEnum[T <: IndexedCaseEnum](implicit s: CaseEnumIndex[T]) =
    ConfigReader[T#Index].map(x => s.caseFromIndex(x).get)
}

package object ppureconfig extends PureConfigSupport
