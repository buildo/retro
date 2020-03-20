package io.buildo.toctoc
package core

import cats.effect.IO

trait CatsIOSupport { self: munit.FunSuite =>

  def await[A, B](test: => IO[Either[A, B]])(implicit loc: munit.Location): B =
    test
      .unsafeRunSync()
      .fold(
        error => fail(error.toString()),
        b => b,
      )

}
