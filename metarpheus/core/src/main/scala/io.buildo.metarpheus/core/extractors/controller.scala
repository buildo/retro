package io.buildo.metarpheus
package core
package extractors

import scala.meta._
import scala.meta.contrib._

package object controller {

  private[this] def extractMethod(m: Decl.Def): String =
    m.mods.collectFirst {
      case Mod.Annot(Init(Name("query"), _, _))   => "get"
      case Mod.Annot(Init(Name("command"), _, _)) => "post"
    }.get

  private[this] val authType = Type.Name("Auth")
  private[this] val operationParametersType = Type.Name("OperationParameters")

  private[this] def isAuthParam(param: scala.meta.Term.Param): Boolean =
    param.decltpe.exists(_.isEqual(authType))

  private[this] def isOperationParameters(param: scala.meta.Term.Param): Boolean =
    param.decltpe.exists(_.isEqual(operationParametersType))

  private[this] def extractAuthenticated(m: Decl.Def): Boolean =
    m.paramss.headOption.exists(_.exists(isAuthParam))

  private[this] def extractParams(
    m: Decl.Def,
    paramsDesc: List[ParamDesc],
    inBody: Boolean,
  ): List[intermediate.RouteParam] = {
    m.paramss.headOption.map { params =>
      params
        .filterNot(isAuthParam)
        .filterNot(isOperationParameters)
        .map { p =>
          val (tpe, required) = p.decltpe.collectFirst {
            case Type.Apply(Type.Name("Option"), Seq(t)) => (tpeToIntermediate(t), false)
            case t: Type                                 => (tpeToIntermediate(t), true)
          }.head

          val name = p.name.syntax
          intermediate.RouteParam(
            name = Option(p.name.syntax),
            tpe = tpe,
            required = required,
            desc = paramsDesc.find(_.name == name).flatMap(_.desc),
            inBody = inBody,
          )
        }
    }.getOrElse(Nil).toList
  }

  private[this] def extractReturnType(m: Decl.Def): intermediate.Type =
    m.decltpe.collect {
      case Type.Apply(Type.Name(_), Seq(Type.Apply(Type.Name(_), Seq(_, tpe)))) =>
        tpeToIntermediate(tpe)
      case Type.Apply(Type.Name(_), Seq(tpe)) =>
        tpeToIntermediate(tpe)
    }.headOption.getOrElse {
      throw new Exception(s"""
                             |This method misses an explicit return type
                             |
                             |  ${m.syntax}
                             |
                             |The return type can be of two types:
                             | - F[E[_, Result]]
                             | - F[Result],
        """.stripMargin)
    }

  private[this] def extractErrorType(m: Decl.Def): Option[intermediate.Type] =
    m.decltpe.collect {
      case Type.Apply(Type.Name(_), Seq(Type.Apply(Type.Name(_), Seq(tpe, _)))) =>
        tpeToIntermediate(tpe)
    }.headOption

  private[this] def extractTraitType(t: Defn.Trait): intermediate.Type =
    intermediate.Type.Apply(t.name.value, t.tparams.map(typeParamToIntermediate))

  def extractAllRoutes(source: Source): List[intermediate.Route] =
    source.collect { case t: Defn.Trait => t }.flatMap(t => extractRoute(source, t))

  def extractRoute(source: Source, t: Defn.Trait): List[intermediate.Route] = {
    val methods = t.collect {
      case m: Decl.Def
          if (m.hasMod(mod"@query") || m.hasMod(mod"@command")) &&
            !m.hasMod(mod"@metarpheusIgnore") =>
        m
    }

    val controllerName = t.name.value
    val pathName = t.mods.collectFirst {
      case Mod.Annot(Init(Name("path"), _, Seq(Seq(Lit.String(n))))) => n
    }
    val pathPrefix = pathName.getOrElse(t.name.value)

    methods.map { m =>
      val scaladoc = findRelatedComment(source, m)
      val (desc, tagsDesc) = extractDescAndTagsFromComment(scaladoc)
      val paramsDesc = tagsDesc.collect { case d: ParamDesc => d }
      val method = extractMethod(m)
      intermediate.Route(
        method = method,
        route = List(
          intermediate.RouteSegment.String(pathPrefix),
          intermediate.RouteSegment.String(m.name.syntax),
        ),
        params = extractParams(m, paramsDesc, inBody = method == "post"),
        authenticated = extractAuthenticated(m),
        returns = extractReturnType(m),
        error = extractErrorType(m),
        pathName = pathName,
        controllerType = extractTraitType(t),
        desc = desc,
        name = List(
          // FIXME: same as a above, for the time being we preserved the `controllerName.method`
          // semantic, but these should really be prefixed using `name` instead of `controllerName`
          controllerName.substring(0, 1).toLowerCase() + controllerName.substring(1),
          m.name.syntax,
        ),
      )
    }
  }

}
