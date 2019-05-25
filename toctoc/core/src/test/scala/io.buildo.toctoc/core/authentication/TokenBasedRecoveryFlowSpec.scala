package io.buildo.toctoc
package core
package authentication

import org.scalatest._
import org.scalatest.prop._
import org.scalactic.TypeCheckedTripleEquals
import org.scalacheck.magnolia._
import cats.effect.IO
import cats.data.EitherT
import java.time.Duration

final class TokenBasedRecoveryFlowSpec
    extends PropSpec
    with PropertyChecks
    with TypeCheckedTripleEquals {

  import TokenBasedAuthentication._
  import TokenBasedRecovery._

  val loginDomain: LoginDomain[IO] =
    InMemoryAuthenticationDomain.create(Map.empty)

  val tokenDomain: AccessTokenDomain[IO] =
    InMemoryAuthenticationDomain.create(Map.empty)

  val recoveryFlow = TokenBasedRecoveryFlow.create(loginDomain, tokenDomain, Duration.ofDays(1))

  def check[A](test: => IO[Either[A, Assertion]]): Assertion =
    test
      .unsafeRunSync()
      .fold(
        error => fail(error.toString()),
        assertion => assertion,
      )

  property("recovery works for existing users") {
    forAll { (s: UserSubject, username: String, password: String) =>
      check {
        (for {
          registerResult <- EitherT(recoveryFlow.registerForRecovery(s))
          (f, token) = registerResult
          f2 <- EitherT(f.recoverLogin(token, password) { _ =>
            IO(Some(username))
          })
          authenticateResult <- EitherT(f2.loginD.authenticate(Login(username, password)))
          (_, subject) = authenticateResult
        } yield {
          assertResult(s.ref)(subject.ref)
        }).value
      }
    }
  }

  property("recovery returns InvalidCredential for non existing users") {
    forAll { (s: UserSubject, password: String) =>
      check {
        (for {
          registerResult <- EitherT(recoveryFlow.registerForRecovery(s))
          (f, token) = registerResult
          result <- EitherT(f.recoverLogin(token, password) { _ =>
            IO(None)
          })
        } yield result).leftMap {
          assertResult(AuthenticationError.InvalidCredential)(_)
        }.swap.value
      }
    }
  }

  property("successful recovery invalidates all existing credentials for a subject") {
    forAll { (s: UserSubject, username: String, password: String) =>
      check {
        (for {
          registerResult <- EitherT(recoveryFlow.registerForRecovery(s))
          (f, token1) = registerResult
          registerResult <- EitherT(recoveryFlow.registerForRecovery(s))
          (f, token2) = registerResult
          f2 <- EitherT(f.recoverLogin(token2, password)(_ => IO(Some(username))))
          authenticateResult <- EitherT(f2.loginD.authenticate(Login(username, password)))
          (_, subject) = authenticateResult
          recoverResult <- EitherT(f.recoverLogin(token1, password)(_ => IO(Some(username))))
        } yield recoverResult).leftMap {
          assertResult(AuthenticationError.InvalidCredential)(_)
        }.swap.value
      }
    }
  }

}
