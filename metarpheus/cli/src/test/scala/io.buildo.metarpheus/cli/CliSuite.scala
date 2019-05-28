package io.buildo.metarpheus
package cli
package test

import org.scalatest._

class CliSuite extends FunSuite {
  val fixturesPath = new java.io.File("metarpheus/core/src/test/resources/fixtures").getAbsolutePath
  test("run main") {
    Cli.main(
      s"--config metarpheus/cli/src/test/resources/fixtures/config.json $fixturesPath".split(" "),
    )
  }

}
