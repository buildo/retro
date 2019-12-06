package io.buildo.tapiro

import io.buildo.metarpheus.core.intermediate.{Type => MetarpheusType}

import scala.meta._

import cats.data.NonEmptyList

object Meta {
  val codecsImplicits = (routes: List[TapiroRoute]) =>
    routes.flatMap {
      case TapiroRoute(route, errorValues) =>
        errorValues.map(m => MetarpheusType.Name(m.name)) ++
          route.body.map(_.tpe) :+
          route.returns
    }.distinct.map(toImplicitParam)

  private[this] val toImplicitParam = (`type`: MetarpheusType) => {
    val typeName = typeNameString(`type`)
    val paramName = Term.Name(s"${typeName.head.toLower}${typeName.tail}")
    val paramType = toScalametaType(`type`)
    param"implicit ${paramName}: JsonCodec[$paramType]"
  }

  val typeName = (`type`: MetarpheusType) => Type.Name(typeNameString(`type`))

  val typeNameString = (`type`: MetarpheusType) =>
    `type` match {
      case MetarpheusType.Apply(name, _) => name
      case MetarpheusType.Name(name)     => name
    }

  val toScalametaType: MetarpheusType => Type = {
    case MetarpheusType.Apply(name, args) =>
      Type.Apply(Type.Name(name), args.map(toScalametaType).toList)
    case MetarpheusType.Name(name) => Type.Name(name)
  }

  def packageFromList(`package`: NonEmptyList[String]): Term.Ref =
    `package`.tail
      .foldLeft[Term.Ref](Term.Name(`package`.head))((acc, n) => Term.Select(acc, Term.Name(n)))
}
