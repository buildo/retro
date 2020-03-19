import io.buildo.enumero._

class CaseEnumSpec extends munit.FunSuite {
  sealed trait Planet extends CaseEnum
  object Planet {
    case object Mercury extends Planet
    case object Venus extends Planet
    case object Earth extends Planet
  }

  test("CaseEnumMacro should construct a sensible CaseEnumSerialization") {
    val serialization = CaseEnumSerialization.caseEnumSerialization[Planet]

    val pairs =
      List(Planet.Mercury -> "Mercury", Planet.Venus -> "Venus", Planet.Earth -> "Earth")

    for ((co, str) <- pairs) {
      assertEquals(serialization.caseToString(co), str)
      assertEquals(serialization.caseFromString(str), Option(co))
    }
  }

  test("SerializationSupport should provide the typeclass instance") {
    trait FakeJsonSerializer[T] {
      def toString(value: T): String
      def fromString(str: String): Either[String, T]
    }

    implicit def fakeJsonSerializer[T <: CaseEnum](implicit instance: CaseEnumSerialization[T]) =
      new FakeJsonSerializer[T] {
        def toString(value: T): String = instance.caseToString(value)
        def fromString(str: String): Either[String, T] =
          instance.caseFromString(str) match {
            case Some(v) => Right(v)
            case None =>
              Left(
                s"$str is not a valid ${instance.name}. Valid values are: ${instance.values.mkString(", ")}",
              )
          }
      }

    assertEquals(
      implicitly[FakeJsonSerializer[Planet]].fromString("Mercury"),
      Right(Planet.Mercury): Either[String, Planet],
    )
    assertEquals(
      implicitly[FakeJsonSerializer[Planet]].fromString("Wrong"),
      Left(
        "Wrong is not a valid Planet. Valid values are: Mercury, Venus, Earth",
      ): Either[String, Planet],
    )
  }

  test("retrieve a typeclass instance using apply") {
    assertEquals(
      CaseEnumSerialization[Planet].caseFromString("Mercury"),
      Option(Planet.Mercury: Planet),
    )
  }

}
