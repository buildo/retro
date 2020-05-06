package mailo.parser

import java.util.regex.Matcher

import cats.syntax.either._
import mailo.parser.ParserError._
import mailo.{MailError, MailRawContent}

import scala.util.matching.Regex
import scala.util.matching.Regex.Match

object HTMLParser {
  def parse(content: MailRawContent, params: Map[String, String]): Either[MailError, String] =
    replaceAllPartials(content.template, content.partials).flatMap(replaceAllParams(_, params))

  private[this] def replaceAllPartials(
    content: String,
    partials: Map[String, String],
  ): Either[MailError, String] = {
    val mockPattern = """\[\[([a-zA-Z0-9_.-]+)\]\]""".r

    val matches = mockPattern.findAllMatchIn(content).map(_.group(1))

    val partialsSet: Set[String] = partials.keySet
    val matchesSet: Set[String] = matches.toSet

    if (matchesSet.subsetOf(partialsSet))
      unsafelyReplaceAllInDocument(content, partials, mockPattern).asRight
    else PartialsDoNotExist(matchesSet -- partialsSet).asLeft
  }

  private[this] def replaceAllParams(
    document: String,
    params: Map[String, String],
  ): Either[MailError, String] = {
    val parameterPattern = """\{\{([a-zA-Z0-9_.-]+)\}\}""".r

    val matches = parameterPattern.findAllMatchIn(document).map(_.group(1))

    val paramsSet: Set[String] = params.keySet
    val matchesSet: Set[String] = matches.toSet

    if (paramsSet == matchesSet)
      unsafelyReplaceAllInDocument(document, params, parameterPattern).asRight[MailError]
    else if (matchesSet.subsetOf(paramsSet))
      TooManyParamsProvided(paramsSet -- matchesSet).asLeft[String]
    else if (paramsSet.subsetOf(matchesSet))
      TooFewParamsProvided(matchesSet -- paramsSet).asLeft[String]
    else if (paramsSet.intersect(matchesSet).isEmpty)
      DisjointParametersAndMatches(paramsSet, matchesSet).asLeft[String]
    else
      OverlappedParametersAndMatches(
        paramsSet -- matchesSet,
        matchesSet -- paramsSet,
        matchesSet.intersect(paramsSet),
      ).asLeft[String]
  }

  private[this] def unsafelyReplaceAllInDocument(
    document: String,
    values: Map[String, String],
    pattern: Regex,
  ): String = {
    def replacement(m: Match): String = {
      require(m.groupCount == 1)
      Matcher.quoteReplacement(values(m.group(1)))
    }

    pattern.replaceAllIn(document, replacement _)
  }
}

object HTMLValidator {
  def validate(document: String): Either[MailError, String] = document.asRight[MailError]
}
