package mailo.parser

import mailo.MailError

object ParserError {
  case object HtmlNotValid extends MailError("Content was not valid HTML")
  case class DisjointParametersAndMatches(
    justParams: Set[String],
    justMatches: Set[String],
  ) extends MailError(
        s"Disjoint parameters and matches, params (${justParams.toString}), matches (${justMatches.toString})",
      )
  case class OverlappedParametersAndMatches(
    justParams: Set[String],
    justMatches: Set[String],
    overlap: Set[String],
  ) extends MailError(
        s"Overlapped parameters and matches, but no exact match: just params (${justParams.toString}), just matches (${justMatches.toString}), overlapped params (${overlap.toString})",
      )
  case class PartialsDoNotExist(partials: Set[String])
      extends MailError(
        s"Some of the provided partials do not exist, here is the list ${partials.toString}",
      )
  case class TooFewPartialsProvided(partials: Set[String])
      extends MailError(
        s"Too few partials provided to the document, here is the list ${partials.toString}",
      )
  case class TooFewParamsProvided(params: Set[String])
      extends MailError(
        s"Too few params provided to the document, here is the list ${params.toString}",
      )
  case class TooManyParamsProvided(params: Set[String])
      extends MailError(
        s"Too many params provided to the document, here is the list ${params.toString}",
      )
}
