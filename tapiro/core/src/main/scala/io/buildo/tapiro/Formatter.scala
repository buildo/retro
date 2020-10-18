package io.buildo.tapiro

import org.scalafmt.Scalafmt
import org.scalafmt.config._
import org.scalafmt.rewrite._

object Formatter {
  val format = (input: meta.Pkg) => {
    Scalafmt
      .format(
        input.syntax,
        ScalafmtConfig.default
          .copy(
            maxColumn = 80,
            rewrite = RewriteSettings(List(PreferCurlyFors)),
            newlines = Newlines(alwaysBeforeTopLevelStatements = true),
          ),
      )
      .get
  }
}
