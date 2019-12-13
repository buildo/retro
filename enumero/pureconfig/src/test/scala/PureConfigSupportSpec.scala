import io.buildo.enumero._
import io.buildo.enumero.ppureconfig._

import org.scalatest.{Matchers, WordSpec}

class CirceSupportSpec extends WordSpec with Matchers {
  sealed trait Planet extends CaseEnum
  object Planet {
    case object Mercury extends Planet
    case object Venus extends Planet
    case object Earth extends Planet
  }
}
