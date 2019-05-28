package io.buildo.metarpheus
package core
package extractors

import scala.meta._

package object model {

  def extractCaseClassDefns(source: scala.meta.Source): List[CaseClassDefnInfo] = {
    source.collect {
      case c: Defn.Class if c.mods.collectFirst {
            case Mod.Case() => ()
          }.isDefined =>
        c
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

  def extractCaseEnumDefns(source: scala.meta.Source): List[CaseEnumDefnInfo] = {
    source.collect {
      case c: Defn.Trait if c.mods.collectFirst {
            case Mod.Annot(Init(Name("enum" | "indexedEnum"), _, _)) => ()
          }.isDefined =>
        c
    }.map { cc =>
      val comment = findRelatedComment(source, cc)
      CaseEnumDefnInfo(SugaredCaseEnumDefns(cc), comment)
    } ++
      source.collect {
        case c =>
          c.children
            .sliding(2)
            .filter {
              case (t: Defn.Trait) :: (o: Defn.Object) :: Nil =>
                t.mods.collectFirst {
                  case _: Mod.Sealed => ()
                }.isDefined &&
                  o.templ.stats.forall {
                    case c: Defn.Object if c.mods.collectFirst {
                          case _: Mod.Case => ()
                        }.isDefined =>
                      true
                    case _ => false
                  }
              case _ => false
            }
            .toList
      }.flatMap(o => o).map {
        case (trait_defn: Defn.Trait) :: (object_defn: Defn.Object) :: Nil =>
          val comment = findRelatedComment(source, trait_defn)
          CaseEnumDefnInfo(VanillaCaseEnumDefns(trait_defn, object_defn), comment)
        case t => throw new RuntimeException(s"Unexpected tree: $t")
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

    val (traitName, members) = caseEnumDefnInfo.defns match {
      case SugaredCaseEnumDefns(defn) =>
        (defn.name.value, membersFromTempl(defn.templ))
      case VanillaCaseEnumDefns(trait_defn, obj_defn) =>
        (trait_defn.name.value, membersFromTempl(obj_defn.templ))
    }

    val (desc, _) = extractDescAndTagsFromComment(caseEnumDefnInfo.commentToken)
    intermediate.CaseEnum(traitName, members, desc)
  }

  def extractModel(source: scala.meta.Source): List[intermediate.Model] =
    extractCaseClassDefns(source).map(extractCaseClass) ++
      extractCaseEnumDefns(source).map(extractCaseEnum(source))

}
