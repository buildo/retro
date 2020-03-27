package mailo

import parser._

class ParserSpec extends munit.FunSuite {
  val template = "{{replaceMe}}{{replaceMeAgain}} [[p1.html]]"
  val partials = Map(
    "p1.html" -> "{{replaceMe}}",
  )

  val params = Map(
    "replaceMe" -> "replacedYou",
    "replaceMeAgain" -> "replacedYouAgain",
  )
  val rawContent = MailRawContent(template, partials)

  test("html parser should be correctly parsed") {
    assertEquals(
      HTMLParser.parse(rawContent, params),
      Right("replacedYoureplacedYouAgain replacedYou"),
    )
  }
}
