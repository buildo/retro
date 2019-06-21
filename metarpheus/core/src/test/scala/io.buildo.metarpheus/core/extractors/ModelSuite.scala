package io.buildo.metarpheus
package core
package test

import org.scalatest._
import ai.x.diff.DiffShow
import ai.x.diff.conversions._

import extractors._

class ModelSuite extends FunSuite {
  lazy val parsed = {
    import scala.meta._
    Fixtures.models.parse[Source].get
  }

  test("extract case classes") {
    val result = model.extractModel(parsed)

    import intermediate._

    val expected =
      List(
        CaseClass(
          name = "CampingName",
          members = List(
            CaseClass.Member(name = "s", tpe = Type.Name("String"), desc = None),
          ),
          desc = None,
          isValueClass = true,
        ),
        CaseClass(
          name = "Camping",
          members = List(
            CaseClass.Member(name = "id", tpe = Type.Name("UUID"), desc = None),
            CaseClass.Member(name = "name", tpe = Type.Name("CampingName"), desc = None),
            CaseClass
              .Member(name = "size", tpe = Type.Name("Int"), desc = Some("number of tents")),
            CaseClass.Member(
              name = "location",
              tpe = Type.Name("CampingLocation"),
              desc = Some("camping location"),
            ),
            CaseClass.Member(
              name = "rating",
              tpe = Type.Name("CampingRating"),
              desc = Some("camping rating"),
            ),
            CaseClass.Member(
              name = "a",
              tpe = Type.Name("A"),
              desc = None,
            ),
          ),
          desc = Some("Represents a camping site"),
          typeParams = List(
            Type.Name("A"),
          ),
        ),
        CaseClass(
          name = "Swan",
          members = List(
            CaseClass.Member(
              name = "color",
              tpe = Type.Name("String"),
              desc = Some("color of the swan"),
            ),
          ),
          desc = Some("Represents a swan"),
        ),
        CaseEnum(
          name = "CampingLocation",
          values = List(
            CaseEnum.Member(
              name = "Seaside",
              desc = Some("Near the sea"),
            ),
            CaseEnum.Member(
              name = "Mountains",
              desc = Some("High up"),
            ),
          ),
          desc = Some("Location of the camping site"),
        ),
        CaseEnum(
          name = "CampingRating",
          values = List(
            CaseEnum.Member(
              name = "High",
              desc = Some("High"),
            ),
            CaseEnum.Member(
              name = "Medium",
              desc = Some("Medium"),
            ),
            CaseEnum.Member(
              name = "Low",
              desc = Some("Low"),
            ),
          ),
          desc = Some("Rating of the camping site"),
        ),
        CaseEnum(
          name = "Planet",
          values = List(
            CaseEnum.Member(
              name = "Earth",
              desc = Some("Earth is a blue planet"),
            ),
            CaseEnum.Member(
              name = "Another",
              desc = Some("Not sure campings exist"),
            ),
          ),
          desc = Some("Planet of the camping site"),
        ),
        TaggedUnion(
          name = "Surface",
          values = List(
            TaggedUnion.Member(
              name = "Sand",
              params = List(),
              desc = Some("Sandy"),
            ),
            TaggedUnion.Member(
              name = "Earth",
              params = List(),
              desc = Some("Dirt"),
            ),
          ),
          desc = Some("Surface of the camping site"),
        ),
        TaggedUnion(
          name = "CreateCampingError",
          values = List(
            TaggedUnion.Member(
              name = "DuplicateName",
              params = List(
                TaggedUnion.MemberParam(
                  name = "names",
                  tpe = Type.Name("SuggestedNames"),
                  desc = Some("suggestions for names that are not in use"),
                ),
              ),
              desc = Some("The name is already in use"),
            ),
            TaggedUnion.Member(
              name = "SizeOutOfBounds",
              params = List(
                TaggedUnion.MemberParam(name = "min", tpe = Type.Name("Int"), desc = None),
                TaggedUnion.MemberParam(name = "max", tpe = Type.Name("Int"), desc = None),
              ),
              desc = Some("The chosen size is not allowed"),
            ),
            TaggedUnion.Member(
              name = "OtherError",
              params = List(),
              desc = None,
            ),
          ),
          desc = Some("Errors that can happen when creating a camping"),
        ),
        CaseClass(
          name = "IgnoreMe",
          members = List(
            CaseClass.Member(name = "ignore", tpe = Type.Name("String"), desc = None),
          ),
          desc = None,
          isValueClass = false,
        ),
        CaseClass(
          name = "DuplicateName",
          members = List(
            CaseClass.Member(
              name = "names",
              tpe = Type.Name("SuggestedNames"),
              desc = Some("suggestions for names that are not in use"),
            ),
          ),
          desc = Some("The name is already in use"),
        ),
        CaseClass(
          name = "SizeOutOfBounds",
          members = List(
            CaseClass.Member(name = "min", tpe = Type.Name("Int"), desc = None),
            CaseClass.Member(name = "max", tpe = Type.Name("Int"), desc = None),
          ),
          desc = Some("The chosen size is not allowed"),
        ),
        CaseClass(
          name = "SuggestedNames",
          members = List(
            CaseClass
              .Member(
                name = "names",
                tpe = Type.Apply("List", List(Type.Name("String"))),
                desc = None,
              ),
          ),
          desc = None,
        ),
      )
    val comparison = DiffShow.diff[List[Model]](expected, result)
    assert(comparison.isIdentical, comparison.string)
  }

}
