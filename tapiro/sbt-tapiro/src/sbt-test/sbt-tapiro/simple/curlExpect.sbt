import scala.sys.process.Process

val curlExpect = InputKey[Unit]("curlExpect")

curlExpect := {
  val args = Def.spaceDelimited("<arg>").parsed
  val curlArgs = args.dropRight(1)
  println("Executing: curl " + curlArgs.mkString(" "))
  val process = Process("curl", curlArgs)
  val expected = args.last
  val out = process.!!
  if (out.trim != expected) sys.error(s"Expected $expected, but got $out")
  ()
}
