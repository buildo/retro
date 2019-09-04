package io.buildo.tapiro

import io.buildo.metarpheus.core.intermediate.{Type => MetarpheusType}

import scala.meta._

import cats.data.NonEmptyList

object Meta {
  val codecsImplicits = (routes: List[TapiroRoute]) => routes.flatMap {
      case TapiroRoute(route, errorValues) =>
        errorValues.map(_.name) ++
          route.body.map(b => typeNameString(b.tpe)) :+
          typeNameString(route.returns)
    }.distinct.map(stringToImplicitParam)

  private[this] val stringToImplicitParam = (name: String) => {
    val paramName = Term.Name(s"${name.head.toLower}${name.tail}")
    val nameType = Type.Name(name)
    param"implicit ${paramName}: JsonCodec[$nameType]"
  }

  val typeName = (`type`: MetarpheusType) => Type.Name(typeNameString(`type`))

  def typeNameString(`type`: MetarpheusType): String =
    `type` match {
      case MetarpheusType.Apply(name, args) =>
        Type.Apply(Type.Name(name), args.map(t => Type.Name(typeNameString(t))).toList).syntax
      case MetarpheusType.Name(name) => name
    }

  def packageFromList(`package`: NonEmptyList[String]): Term.Ref =
    `package`.tail.foldLeft[Term.Ref](Term.Name(`package`.head))((acc, n) => Term.Select(acc, Term.Name(n)))
}
