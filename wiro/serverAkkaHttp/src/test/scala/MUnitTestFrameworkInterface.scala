package wiro

import akka.http.scaladsl.testkit.TestFrameworkInterface
import akka.http.scaladsl.server.ExceptionHandler

trait MUnitTestFrameworkInterface extends TestFrameworkInterface { self: munit.FunSuite =>
  override def afterAll(): Unit = {
    cleanUp()
  }

  override def failTest(msg: String): Nothing = fail(msg)

  def testExceptionHandler: ExceptionHandler = ExceptionHandler { case e =>
    throw e
  }
}
