package io.buildo.tapiro

import io.buildo.metarpheus.core.intermediate.{Type => MetarpheusType}

import scala.meta._

import cats.data.NonEmptyList

object Meta {
  val codecsImplicits = (routes: List[TapiroRoute]) => {
    val jsonCodecs = routes.flatMap {
      case TapiroRoute(route, errorValues) =>
        val params: List[MetarpheusType] = route.params.map(_.tpe)
        errorValues.map(m => MetarpheusType.Name(m.name)) ++
          (if (route.method == "post") params else Nil) ++
          route.body.map(_.tpe) :+
          route.returns
    }.distinct.map(toJsonCodec)
    val plainCodecs = routes.flatMap {
      case TapiroRoute(route, _) =>
        (if (route.method == "get") route.params.map(_.tpe) else Nil)
    }.distinct.map(toPlainCodec)
    jsonCodecs ++ plainCodecs
  }

  private[this] val toJsonCodec = (`type`: MetarpheusType) => {
    val typeName = typeNameString(`type`)
    val paramName = Term.Name(s"${typeName.head.toLower}${typeName.tail}JsonCodec")
    val paramType = toScalametaType(`type`)
    param"implicit ${paramName}: JsonCodec[$paramType]"
  }

  private[this] val toPlainCodec = (`type`: MetarpheusType) => {
    val typeName = typeNameString(`type`)
    val paramName = Term.Name(s"${typeName.head.toLower}${typeName.tail}PlainCodec")
    val paramType = toScalametaType(`type`)
    param"implicit ${paramName}: PlainCodec[$paramType]"
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
