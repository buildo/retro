import io.buildo.enumero._
import io.buildo.enumero.circe._
import io.circe.syntax._
import io.circe.parser.parse

class CirceSupportSuite extends munit.FunSuite {

  sealed trait Planet extends CaseEnum
  object Planet {
    case object Mercury extends Planet
    case object Venus extends Planet
    case object Earth extends Planet
  }

  val planetMap = Map[Planet, Int](
    Planet.Mercury -> 12,
    Planet.Venus -> 812763,
    Planet.Earth -> 0,
  )

  val planetMapJson = parse("""
      {
        "Mercury": 12,
        "Venus": 812763,
        "Earth": 0
      }
    """).right.get

  test("CirceSupport handles encoding a map with CaseEnum keys") {
    val encodedJson = planetMap.asJson
    assertEquals(encodedJson, planetMapJson)
  }

  test("CirceSupport handles decoding a json with CaseEnum keys") {
    assertEquals(planetMapJson.as[Map[Planet, Int]].right.get, planetMap)
  }

  test("CirceSupport handles decoding a json with wrong CaseEnum keys") {
    val decodeResult = parse("""
      {
        "Mercury": 12,
        "Venus": 812763,
        "wrongKey": 0
      }
    """).right.get
      .as[Map[Planet, Int]]
    assert(decodeResult.isLeft)
  }
}
