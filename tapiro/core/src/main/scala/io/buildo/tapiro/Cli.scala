package io.buildo.tapiro

import scopt.OParser
import cats.data.NonEmptyList

case class CliConfig(
  from: String = "/Users/cale/tmp",
  to: String = "./",
  `package`: String = "tapiro",
  includeHttp4sModels: Boolean = true,
)

//[Deprecated] the cli is going to be deprecated
object Cli {
  def main(args: Array[String]): Unit = {
    val builder = OParser.builder[CliConfig]
    val parser = {
      import builder._
      OParser.sequence(
        programName("tapiro"),
        head("tapiro", "0.0.1"),
        opt[String]('f', "from")
          .action((x, c) => c.copy(from = x))
          .text("from is a string property"),
        opt[String]('t', "to")
          .action((x, c) => c.copy(to = x))
          .text("to is a string property"),
        opt[String]('p', "package")
          .action((x, c) => c.copy(`package` = x))
          .text("package is a string property"),
        opt[Boolean]('h', "Http4sModule")
          .action((x, c) => c.copy(includeHttp4sModels = x))
          .text("Http4sModule is a boolean property"),
      )
    }

    OParser.parse(parser, args, CliConfig()) match {
      case Some(c) => {
        c.`package`.split(".").toList match {
          case Nil => throw new Exception("Cannot create routes with empty package")
          case head :: tail => Util.createFiles(c.from, c.to, NonEmptyList(head, tail), c.includeHttp4sModels)
        }
      }
      case _ =>
        println("Couldn't read the configurations")
    }
  }
}
