package io.buildo.metarpheus.core.extractors

import scala.meta._

private[extractors] sealed trait Tag
private[extractors] case class ParamDesc(name: String, desc: Option[String]) extends Tag
private[extractors] case class PathParamDesc(name: String, desc: Option[String]) extends Tag
private[extractors] case class RouteName(name: List[String]) extends Tag

case class CaseClassDefnInfo(defn: Defn.Class, commentToken: Option[scala.meta.Token])

sealed trait CaseEnumDefns
case class SugaredCaseEnumDefns(defn: Defn.Trait) extends CaseEnumDefns
case class VanillaCaseEnumDefns(trait_defn: Defn.Trait, obj_defn: Defn.Object) extends CaseEnumDefns

case class CaseEnumDefnInfo(defns: CaseEnumDefns, commentToken: Option[scala.meta.Token])
