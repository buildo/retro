import scala.sys.process.Process

val waitIsUp = InputKey[Unit]("waitIsUp")

waitIsUp := {
  val args = Def.spaceDelimited("<arg>").parsed
  val site = args(0)
  def checkIsUp(times: Int): Unit = {
    if (times <= 0) {
      sys.error(s"$site didn't come up in time")
    }
    println(s"Checking if $site is up...")
    val process = Process("curl", List("-s", "-o", "/dev/null", site))
    val isUp = process.! == 0
    if (!isUp) {
      Thread.sleep(500)
      checkIsUp(times - 1)
    }
    ()
  }
  checkIsUp(times = 15)
  ()
}
