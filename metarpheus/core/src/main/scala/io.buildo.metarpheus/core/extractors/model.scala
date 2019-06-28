package io.buildo.metarpheus
package core
package extractors

import scala.meta._
import scala.meta.contrib._

package object model {

  def extractCaseClassDefns(source: scala.meta.Source): List[CaseClassDefnInfo] = {
    source.collect {
      case c: Defn.Class if c.hasMod(Mod.Case()) => c
    }.map { cc =>
      val comment = findRelatedComment(source, cc)
      CaseClassDefnInfo(cc, comment)
    }
  }

  /**
    * Extract the intermediate representation for a case class from the output
    * of extractCaseClassDefns
    */
  def extractCaseClass(caseClassDefnInfo: CaseClassDefnInfo): intermediate.CaseClass = {
    val CaseClassDefnInfo(defn, comment) = caseClassDefnInfo
    val isValueClass = defn.templ.inits.exists(_.syntax == "AnyVal")
    val className = defn.name.value
    val Ctor.Primary(_, _: Name.Anonymous, List(plist)) = defn.ctor
    val (classDesc, tags) = extractDescAndTagsFromComment(comment)
    // FIXME fail if unmatched parameter descriptions are found
    val paramDescs = tags.collect { case p: ParamDesc => p }
    val members = plist.map {
      case Term.Param(_, Term.Name(name), Some(tpe: scala.meta.Type), _) =>
        intermediate.CaseClass.Member(
          name = name,
          tpe = tpeToIntermediate(tpe),
          desc = paramDescs.find(_.name == name).flatMap(_.desc),
        )
    }.toList
    val typeParams = defn.tparams.map(typeParamToIntermediate).toList
    intermediate.CaseClass(
      name = className,
      typeParams = typeParams,
      members = members,
      desc = classDesc,
      isValueClass = isValueClass,
    )
  }

  def extractCaseEnumDefns(source: scala.meta.Source): List[CaseEnumDefnInfo] =
    source.collect {
      case c: Defn.Trait if c.mods.collectFirst {
            case Mod.Annot(Init(Name("enum" | "indexedEnum"), _, _)) => ()
          }.isDefined =>
        c
    }.map { cc =>
      val comment = findRelatedComment(source, cc)
      CaseEnumDefnInfo(cc, comment)
    }

  def extractTaggedUnionDefns(source: Source): List[TaggedUnionDefnInfo] = {

    def checkExtends(templ: Template, t: Defn.Trait): Boolean =
      templ.inits.exists(_.syntax == t.name.value)

    val sealedTraits = source.collect {
      case t: Defn.Trait if t.hasMod(Mod.Sealed()) => t
    }
    val members = source.collect {
      case d: Defn.Object if d.hasMod(Mod.Case()) => d
      case d: Defn.Class if d.hasMod(Mod.Case())  => d
    }

    sealedTraits.map { t =>
      TaggedUnionDefnInfo(
        traitDefn = t,
        memberDefns = members.collect {
          case d: Defn.Object if checkExtends(d.templ, t) => d
          case d: Defn.Class if checkExtends(d.templ, t)  => d
        },
        commentToken = findRelatedComment(source, t),
      )
    }
  }

  /**
    * Extract the ADT-like enumeration intermediate representation from the output of
    * of extractCaseEnumDefns
    */
  def extractCaseEnum(
    source: scala.meta.Source,
  )(caseEnumDefnInfo: CaseEnumDefnInfo): intermediate.CaseEnum = {

    def membersFromTempl(t: Template): List[intermediate.CaseEnum.Member] = {
      t.stats.collect {
        case o @ Defn.Object(_, Term.Name(memberName), _) => {
          val comment = findRelatedComment(source, o)
          val (memberDesc, _) = extractDescAndTagsFromComment(comment)
          intermediate.CaseEnum.Member(memberName, memberDesc)
        }
        case o @ Term.Name(memberName) => {
          val comment = findRelatedComment(source, o)
          val (memberDesc, _) = extractDescAndTagsFromComment(comment)
          intermediate.CaseEnum.Member(memberName, memberDesc)
        }
      }.toList
    }

    val traitName = caseEnumDefnInfo.defn.name.value
    val members = membersFromTempl(caseEnumDefnInfo.defn.templ)

    val (desc, _) = extractDescAndTagsFromComment(caseEnumDefnInfo.commentToken)
    intermediate.CaseEnum(traitName, members, desc)
  }

  /**
    * Extract the intermediate representation of the tagged union from the output
    * of extractTaggedUnionDefns
    */
  def extractTaggedUnion(
    source: scala.meta.Source,
  )(taggedUnionDefnInfo: TaggedUnionDefnInfo): intermediate.TaggedUnion = {

    def toMember(d: Defn): intermediate.TaggedUnion.Member = {
      val commentToken = findRelatedComment(source, d)
      d match {
        case c: Defn.Class =>
          val (memberDesc, tags) = extractDescAndTagsFromComment(commentToken)
          // FIXME fail if unmatched parameter descriptions are found
          val paramDescs = tags.collect { case p: ParamDesc => p }
          val Ctor.Primary(_, _: Name.Anonymous, List(plist)) = c.ctor
          val memberParams = plist.map {
            case Term.Param(_, Term.Name(name), Some(tpe: scala.meta.Type), _) =>
              intermediate.CaseClass.Member(
                name = name,
                tpe = tpeToIntermediate(tpe),
                desc = paramDescs.find(_.name == name).flatMap(_.desc),
              )
          }
          intermediate.TaggedUnion.Member(
            name = c.name.value,
            params = memberParams,
            desc = memberDesc,
            isValueClass = c.templ.inits.exists(_.syntax == "AnyVal"),
          )
        case o: Defn.Object =>
          intermediate.TaggedUnion.Member(
            name = o.name.value,
            params = List(),
            desc = extractDescAndTagsFromComment(commentToken)._1,
            isValueClass = false,
          )
      }
    }

    val traitName = taggedUnionDefnInfo.traitDefn.name.value
    val members = taggedUnionDefnInfo.memberDefns.map(toMember)

    val (desc, _) = extractDescAndTagsFromComment(taggedUnionDefnInfo.commentToken)
    intermediate.TaggedUnion(traitName, members, desc)
  }

  def extractModel(source: scala.meta.Source): List[intermediate.Model] =
    extractCaseClassDefns(source).map(extractCaseClass) ++
      extractCaseEnumDefns(source).map(extractCaseEnum(source)) ++
      extractTaggedUnionDefns(source).map(extractTaggedUnion(source))

}
