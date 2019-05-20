package mailo

import org.scalatest.{ FlatSpec, Matchers }
import org.scalatest.concurrent.ScalaFutures

import cats.syntax.either._

import parser._

class ParserSpec extends FlatSpec with AppSpec with Matchers {
  val template = "{{replaceMe}}{{replaceMeAgain}} [[p1.html]]"
  val partials = Map(
    "p1.html" -> "{{replaceMe}}"
  )

  val params = Map(
    "replaceMe" -> "replacedYou",
    "replaceMeAgain" -> "replacedYouAgain"
  )
  val rawContent = MailRawContent(template, partials)

  "html parser" should "be correctly parsed" in {
    HTMLParser.parse(rawContent, params) should be (Right("replacedYoureplacedYouAgain replacedYou"))
  }
}
