package io.buildo.tapiro

import io.buildo.metarpheus.core.intermediate.{TaggedUnion, RouteParam, Type => MetarpheusType}

import scala.meta._
import scala.meta.contrib._

import cats.data.NonEmptyList

object Meta {
  val codecsImplicits = (routes: List[TapiroRoute], authTokenName: String) => {
    val notUnit = (t: MetarpheusType) => t != MetarpheusType.Name("Unit")
    val toDecoder = (t: Type) => t"Decoder[${extractListType(t)}]"
    val toEncoder = (t: Type) => t"Encoder[${extractListType(t)}]"
    val toJsonCodec = (t: Type) => t"JsonCodec[${extractListType(t)}]"
    val toPlainCodec = (t: Type) => t"PlainCodec[${extractListType(t)}]"
    val routeRequiredImplicits = (route: TapiroRoute) => {
      val (authParamTypes, nonAuthParamTypes) =
        route.route.params.map(_.tpe).partition(isAuthToken(_, authTokenName))
      val inputImplicits =
        route.method match {
          case RouteMethod.GET =>
            nonAuthParamTypes.map(metarpheusTypeToScalametaType).map(toPlainCodec)
          case RouteMethod.POST =>
            nonAuthParamTypes
              .map(metarpheusTypeToScalametaType)
              .flatMap(t => List(toDecoder(t), toEncoder(t)))
        }
      val outputImplicits =
        List(route.route.returns)
          .filter(notUnit)
          .map(metarpheusTypeToScalametaType)
          .map(toJsonCodec)
      val errorImplicits =
        route.error match {
          case RouteError.TaggedUnionError(tu) =>
            tu.values.map(taggedUnionMemberType(tu)).map(toJsonCodec)
          case RouteError.OtherError(t) =>
            List(t).filter(notUnit).map(metarpheusTypeToScalametaType).map(toJsonCodec)
        }
      val authImplicits = authParamTypes.map(metarpheusTypeToScalametaType).map(toPlainCodec)
      inputImplicits ++ outputImplicits ++ errorImplicits ++ authImplicits
    }
    deduplicate(routes.flatMap(routeRequiredImplicits)).zipWithIndex.map(toImplicitParam.tupled)
  }

  private[this] def extractListType(t: Type): Type = t match {
    case Type.Apply(Type.Name("List"), args) => args.head
    case _                                   => t
  }

  private[this] val deduplicate: List[Type] => List[Type] = (ts: List[Type]) =>
    ts match {
      case Nil          => Nil
      case head :: tail => head :: deduplicate(tail.filter(!_.isEqual(head)))
    }

  private[this] val isAuthToken = (t: MetarpheusType, authTokenName: String) =>
    t == MetarpheusType.Name(authTokenName)

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

  val authTypeString = (`type`: MetarpheusType) =>
    `type` match {
      case MetarpheusType.Apply(_, args) =>
        args.last match {
          case MetarpheusType.Name(name)  => Some(name)
          case MetarpheusType.Apply(_, _) => None
        }
      case MetarpheusType.Name(_) => None
    }

  val metarpheusTypeToScalametaType: MetarpheusType => Type = {
    case MetarpheusType.Apply(name, args) =>
      Type.Apply(Type.Name(name), args.map(metarpheusTypeToScalametaType).toList)
    case MetarpheusType.Name(name) => Type.Name(name)
  }

  val routeParamToScalametaType = (routeParam: RouteParam) => {
    val t = metarpheusTypeToScalametaType(routeParam.tpe)
    if (routeParam.required) t else t"Option[$t]"
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

  val toEndpointImplementation = (route: TapiroRoute, authTokenName: String) => {
    val name = Term.Name(route.route.name.last)
    val controllersName = q"controller.$name"
    route.method match {
      case RouteMethod.GET =>
        route.route.params.length match {
          case 0 => q"_ => $controllersName()"
          case 1 => controllersName
          case _ => q"($controllersName _).tupled"
        }
      case RouteMethod.POST =>
        val fields = route.route.params
          .filterNot(_.tpe == MetarpheusType.Name(authTokenName))
          .map(p => Term.Name(p.name.getOrElse(Meta.typeNameString(p.tpe))))
        val hasAuth = route.route.params
          .exists(_.tpe == MetarpheusType.Name(authTokenName))
        if (hasAuth)
          q"{ case (x, token) => $controllersName(..${fields.map(f => q"x.$f")}, token) }"
        else
          q"x => $controllersName(..${fields.map(f => q"x.$f")})"
    }
  }
}
