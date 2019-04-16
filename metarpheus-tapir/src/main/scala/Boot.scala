import scopt.OParser

case class CliConfig(
  from: String = "/Users/cale/tmp",
  to: String = "./Endpoints.scala",
  `package`: String = "tapiro",
)

object Boot {
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
      )
    }

    OParser.parse(parser, args, CliConfig()) match {
      case Some(c) =>
        Util.createFile(c.from, c.to, c.`package`)
      case _ =>
        println("Couldn't read the configurations")
    }
  }
}