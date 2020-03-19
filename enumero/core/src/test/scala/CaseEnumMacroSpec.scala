import io.buildo.enumero.annotations.{enum, indexedEnum}

class CaseEnumMacroSuite extends munit.FunSuite {
  @enum trait Planet {
    Mercury
    Venus
    Earth
  }

  @enum trait Beer {
    object Lager
    object Ale
  }

  test("@enum annotation should produce a valid CaseEnum-style ADT") {
    assert(Planet.Earth.isInstanceOf[Product])
    assert(Planet.Earth.isInstanceOf[Serializable])
    assert(Planet.Earth.isInstanceOf[Planet])
    assertEquals(Planet.Earth, Planet.Earth)
  }

  test("@enum annotation should produce a valid CaseEnum-style ADT (alternative syntax)") {
    assert(Beer.Lager.isInstanceOf[Product])
    assert(Beer.Lager.isInstanceOf[Serializable])
    assert(Beer.Lager.isInstanceOf[Beer])
    assertEquals(Beer.Lager, Beer.Lager)
  }

  test("@enum annotation should allow accessing the values of the enumeration") {
    (Planet.values: Set[Planet]) // typecheck
    assertEquals(Planet.values, Set(Planet.Mercury, Planet.Venus, Planet.Earth))
  }

  test("@enum annotation should allow printing / parsing the values of the enumeration") {
    assertEquals(Planet.caseFromString("Earth"), Option(Planet.Earth: Planet))
    assertEquals(Planet.caseFromString("Nope"), None: Option[Planet])
    assertEquals(Planet.caseToString(Planet.Earth), "Earth")
    compileErrors("Planet.caseToString(Beer.Lager)")
  }

  test("@enum annotation should allow accessing the enumeration name") {
    assertEquals(Planet.name, "Planet")
  }

}

class IndexedCaseEnumMacroSpec extends munit.FunSuite {
  @indexedEnum trait Planet {
    type Index = Int
    Mercury { 1 }
    Venus { 2 }
    Earth { 3 }
  }

  @indexedEnum trait Beer {
    type Index = Int
    Lager { 1 }
    Ale { 2 }
  }

  test("@indexedEnum annotation should produce a valid IndexedCaseEnum-style ADT") {
    val _: Int = 3: Planet#Index // typecheck
    assert(Planet.Earth.isInstanceOf[Product])
    assert(Planet.Earth.isInstanceOf[Serializable])
    assert(Planet.Earth.isInstanceOf[Planet])
    assertEquals(Planet.Earth, Planet.Earth)
    assertEquals(Planet.Earth.index, 3)
  }

  test(
    "@indexedEnum annotation should produce a valid IndexedCaseEnum-style ADT (alternative syntax)",
  ) {
    val _: Int = 2: Planet#Index // typecheck
    assert(Beer.Lager.isInstanceOf[Product])
    assert(Beer.Lager.isInstanceOf[Serializable])
    assert(Beer.Lager.isInstanceOf[Beer])
    assertEquals(Beer.Lager, Beer.Lager)
    assertEquals(Beer.Ale.index, 2)
  }

}
