package io.buildo.tapiro

import io.buildo.metarpheus.core.intermediate.{TaggedUnion, Type => MetarpheusType}

import scala.meta._

import cats.data.NonEmptyList

object Meta {
  val codecsImplicits = (routes: List[TapiroRoute]) => {
    val jsonCodecs = (routes.flatMap {
      case TapiroRoute(route, error) =>
        val params: List[MetarpheusType] = route.params.map(_.tpe)
        ((if (route.method == "post") params else Nil) ++
          (error match {
            case TapiroRouteError.OtherError(t) => List(t)
            case _                              => Nil
          }) ++
          route.body.map(_.tpe) :+
          route.returns)
    }.distinct
    //no json codec for Unit, AuthToken in tapir
      .filter(t => typeNameString(t) != "Unit")
      .filter(t => typeNameString(t) != "AuthToken")
      .map(toScalametaType)
      ++ taggedUnionErrorMembers(routes))
      .map(t => t"JsonCodec[$t]")
    val plainCodecs = routes.flatMap {
      case TapiroRoute(route, _) =>
        (if (route.method == "get") route.params.map(_.tpe) else Nil) ++
          route.params.map(_.tpe).filter(typeNameString(_) == "AuthToken")
    }.distinct.map(t => t"PlainCodec[${toScalametaType(t)}]")
    val codecs = jsonCodecs ++ plainCodecs
    codecs.zipWithIndex.map(toImplicitParam.tupled)
  }

  private[this] val taggedUnionErrorMembers = (routes: List[TapiroRoute]) => {
    val taggedUnions = routes.collect {
      case TapiroRoute(_, TapiroRouteError.TaggedUnionError(tu)) => tu
    }.distinct
    taggedUnions.flatMap { taggedUnion =>
      taggedUnion.values.map(taggedUnionMemberType(taggedUnion))
    }
  }

  private[this] val toImplicitParam = (paramType: Type, index: Int) => {
    val paramName = Term.Name(s"codec$index")
    param"implicit $paramName: $paramType"
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

  val taggedUnionMemberType = (taggedUnion: TaggedUnion) =>
    (member: TaggedUnion.Member) => {
      if (member.params.isEmpty)
        Type.Singleton(Term.Select(Term.Name(taggedUnion.name), Term.Name(member.name)))
      else Type.Select(Term.Name(taggedUnion.name), Type.Name(member.name)),
    }

  def packageFromList(`package`: NonEmptyList[String]): Term.Ref =
    `package`.tail
      .foldLeft[Term.Ref](Term.Name(`package`.head))((acc, n) => Term.Select(acc, Term.Name(n)))
}
