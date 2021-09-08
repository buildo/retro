package io.buildo.metarpheus
package core

import scala.meta.{Syntax, Term, Type}
import scala.meta.contrib.AssociatedComments

package object extractors {

  def extractFullAPI(
    parsed: List[scala.meta.Source],
  ): intermediate.API = {

    val models: List[intermediate.Model] =
      parsed.flatMap(extractors.model.extractModel)

    val routes: List[intermediate.Route] =
      parsed.flatMap(extractors.controller.extractAllRoutes)

    intermediate.API(models, routes)
  }

  /*
   * Convert a scala-meta representation of a type to a metarpheus
   * intermediate representation
   */
  private[extractors] def tpeToIntermediate(tpe: Type): intermediate.Type = tpe match {
    case name: scala.meta.Type.Name =>
      intermediate.Type.Name(name.value)
    case scala.meta.Type.Apply(name: scala.meta.Type.Name, args) =>
      intermediate.Type.Apply(name.value, args.map(tpeToIntermediate))
    case scala.meta.Type.Select(_, t) => tpeToIntermediate(t)
  }

  private[extractors] def tpeToIntermediate(t: Term.ApplyType): intermediate.Type = t match {
    case Term.ApplyType(name: Term.Name, _) =>
      intermediate.Type.Apply(name.value, t.targs.map(tpeToIntermediate))
  }

  private[extractors] def typeParamToIntermediate(t: Type.Param): intermediate.Type =
    t.tparams match {
      case Nil     => intermediate.Type.Name(t.name.value)
      case tparams => intermediate.Type.Apply(t.name.value, tparams.map(typeParamToIntermediate))
    }

  private[extractors] def stripCommentMarkers(s: String) =
    s.stripPrefix("/")
      .dropWhile(_ == '*')
      .reverse
      .stripPrefix("/")
      .dropWhile(_ == '*')
      .reverse

  /*
   * Search for the comment associated with this definition
   */
  private[extractors] def findRelatedComment(
    source: scala.meta.Source,
    t: scala.meta.Tree,
  ): Option[scala.meta.Token] =
    AssociatedComments(source.tokens).leading(t).headOption

  /**
    * Extract route description and tags (such as @param) from route comment
    */
  private[extractors] def extractDescAndTagsFromComment(
    token: Option[scala.meta.Token],
  ): (Option[String], List[Tag]) =
    token.map { c =>
      val cleanLines = stripCommentMarkers(c.show[Syntax])
        .split("\n")
        .map(_.trim.stripPrefix("*").trim)
        .filter(_ != "")
        .toList

      val TagRegex = """@([^\s]+) (.*)""".r
      val ParamRegex = """@param ([^\s]+) (.+)""".r
      val ParamRegexNoDesc = """@param ([^\s]+)""".r
      val PathParamRegex = """@pathParam ([^\s]+) (.+)""".r
      val PathParamRegexNoDesc = """@pathParam ([^\s]+)""".r
      val RouteNameRegex = """@name ([^\s]+)""".r

      val (desc, tagLines) = cleanLines.span(_ match {
        case TagRegex(_, _) => false
        case _              => true
      })

      @annotation.tailrec
      def getTags(acc: List[Tag], lines: List[String]): List[Tag] = lines match {
        case Nil => acc
        case l :: ls => {
          val (tagls, rest) = ls.span(_ match {
            case TagRegex(_, _) => false
            case _              => true
          })
          val next = l match {
            case ParamRegex(name, l1)   => ParamDesc(name, Some((l1 :: tagls).mkString(" ")))
            case ParamRegexNoDesc(name) => ParamDesc(name, None)
            case PathParamRegex(name, l1) =>
              PathParamDesc(name, Some((l1 :: tagls).mkString(" ")))
            case PathParamRegexNoDesc(name) => PathParamDesc(name, None)
            case RouteNameRegex(name)       => RouteName(name.split("""\.""").toList)
          }
          getTags(acc :+ next, rest)
        }
      }

      (Some(desc.mkString(" ")), getTags(Nil, tagLines))
    }.getOrElse((None, List()))

  private[extractors] def flattenPackage(pkg: scala.meta.Pkg): List[String] =
    pkg.ref match {
      case Term.Name(name) => List(name)
      case s: Term.Select  => flattenSelect(s)
    }

  private[extractors] def flattenSelect(s: Term.Select): List[String] =
    s.qual match {
      case sel: Term.Select => flattenSelect(sel) ++ List(s.name.value)
      case Term.Name(n)     => List(n, s.name.value)
    }

  private[extractors] def extractPackage(source: scala.meta.Source): List[String] =
    source.collect { case pkg: scala.meta.Pkg =>
      flattenPackage(pkg)
    }.flatten match {
      case Nil => List("_root_")
      case x   => x
    }
}
