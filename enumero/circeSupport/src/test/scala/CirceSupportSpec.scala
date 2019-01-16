import io.buildo.enumero._
import io.buildo.enumero.circe._
import io.circe._, io.circe.syntax._, io.circe.parser._

import org.scalatest.{Matchers, WordSpec}

class CirceSupportSpec extends WordSpec with Matchers {
  sealed trait Planet extends CaseEnum
  object Planet {
    case object Mercury extends Planet
    case object Venus extends Planet
    case object Earth extends Planet
  }

  val planetMap = Map[Planet, Int](
    Planet.Mercury -> 12,
    Planet.Venus -> 812763,
    Planet.Earth -> 0
  )

  val json = planetMap.asJson

  "CirceSupport handles encoding of a map with CaseEnum keys" in {
    json shouldBe parse("""
      {
        "Mercury": 12,
        "Venus": 812763,
        "Earth": 0
      }
    """).getOrElse(Json.Null)
  }

  "CirceSupport handles dencoding of a json with CaseEnum keys" in {
    json.as[Map[Planet, Int]].getOrElse(Json.Null) shouldBe planetMap
  }

  "CirceSupport handles dencoding of a json with wrong CaseEnum keys" in {
    parse("""
      {
        "Mercury": 12,
        "Venus": 812763,
        "wrongKey": 0
      }
    """)
      .getOrElse(Json.Null)
      .as[Map[Planet, Int]] shouldBe a[Left[_, DecodingFailure]]
  }
}
