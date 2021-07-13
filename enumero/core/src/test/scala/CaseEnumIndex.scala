import io.buildo.enumero._
import scala.language.reflectiveCalls

class CaseEnumIndexSuite extends munit.FunSuite {
  sealed trait Planet extends IndexedCaseEnum { type Index = Int }
  object Planet {
    case object Mercury extends Planet { val index = 1 }
    case object Venus extends Planet { val index = 2 }
    case object Earth extends Planet { val index = 3 }
  }

  test("CaseEnumIndexMacro should construct a sensible CaseEnumIndex") {
    val converter = CaseEnumIndex.caseEnumIndex[Planet]

    val pairs = List(Planet.Mercury -> 1, Planet.Venus -> 2, Planet.Earth -> 3)

    for ((co, index) <- pairs) {
      assertEquals(converter.caseToIndex(co), index)
      assertEquals(converter.caseFromIndex(index), Some(co))
    }
  }

  test("CaseEnumIndex should provide the typeclass instance") {
    trait FakeBinaryPickler[T] {
      def pickle(c: T)(picklerState: { def writeInt(int: Int): Unit }): Unit
      def unpickle(unpicklerState: { def getInt(): Int }): Option[T]
    }

    implicit def fakeBinaryPickler[T <: IndexedCaseEnum { type Index = Int }](implicit
      instance: CaseEnumIndex[T],
    ) = new FakeBinaryPickler[T] {

      def pickle(c: T)(picklerState: { def writeInt(int: Int): Unit }): Unit = {
        picklerState.writeInt(instance.caseToIndex(c))
      }
      def unpickle(unpicklerState: { def getInt(): Int }): Option[T] = {
        instance.caseFromIndex(unpicklerState.getInt())
      }
    }

    object picklerState {
      var value: Int = 0
      def writeInt(int: Int): Unit = {
        value = int
      }
    }
    val binaryPickler = implicitly[FakeBinaryPickler[Planet]]
    binaryPickler.pickle(Planet.Venus)(picklerState)
    assertEquals(picklerState.value, 2)

    object unpicklerState {
      def getInt(): Int = 3
    }
    assertEquals(binaryPickler.unpickle(unpicklerState), Some(Planet.Earth))
  }

  test("CaseEnumIndex should retrieve a typeclass instance using apply") {
    assertEquals(CaseEnumIndex[Planet].caseFromIndex(1), Some(Planet.Mercury))
  }

}
