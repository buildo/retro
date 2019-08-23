package io.buildo.tapiro

import io.buildo.metarpheus.core.intermediate.{Type => MetarpheusType}

import scala.meta._

object Meta {
  val codecsImplicits = (routes: List[TapiroRoute]) => {
    routes.map {
      case TapiroRoute(route, errorValues) =>
        (List(route.returns) ++ route.body.map(_.tpe)).map(typeToImplicitParam) ++
          errorValues.map(error => stringToImplicitParam(error.name))
    }.flatten.distinct,
  }

  private[this] val typeToImplicitParam = (tpe: MetarpheusType) =>
    stringToImplicitParam(typeNameString(tpe))

  private[this] val stringToImplicitParam = (name: String) => {
    val paramName = Term.Name(s"${name.head.toLower}${name.tail}")
    val nameType = Type.Name(name)
    param"implicit ${paramName}: JsonCodec[$nameType]"
  }

  val typeName = (`type`: MetarpheusType) => Type.Name(typeNameString(`type`))

  val typeNameString = (`type`: MetarpheusType) =>
    `type` match {
      case MetarpheusType.Apply(name, _) => name
      case MetarpheusType.Name(name)     => name
    }
}
