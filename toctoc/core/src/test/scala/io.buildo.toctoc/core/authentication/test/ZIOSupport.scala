package io.buildo.toctoc
package core
package authentication
package test

import zio.{IO, Runtime, Unsafe}

trait ZIOSupport { self: munit.FunSuite =>

  implicit val runtime: Runtime[Any] = Runtime.default

  def await[A, B](test: IO[A, B])(implicit loc: munit.Location): B =
    Unsafe.unsafe { implicit unsafe =>
      runtime.unsafe.run(test).foldExit(error => fail(error.toString()), b => b)
    }
}
