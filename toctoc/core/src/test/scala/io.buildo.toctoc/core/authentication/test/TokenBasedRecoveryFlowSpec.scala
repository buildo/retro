package io.buildo.toctoc
package core
package authentication
package test

import java.time.Duration

import zio.ZIO
import org.scalacheck.Prop.forAll
import org.scalacheck.Arbitrary

final class TokenBasedRecoveryFlowSpec extends munit.ScalaCheckSuite with ZIOSupport {

  import TokenBasedAuthentication._
  import TokenBasedRecovery._

  val loginDomain: LoginDomain =
    InMemoryAuthenticationDomain.create(Map.empty)

  val tokenDomain: AccessTokenDomain =
    InMemoryAuthenticationDomain.create(Map.empty)

  val recoveryFlow = TokenBasedRecoveryFlow.create(loginDomain, tokenDomain, Duration.ofDays(1))

  implicit val userSubjectArbitrary: Arbitrary[UserSubject] = Arbitrary {
    Arbitrary.arbitrary[String].map(UserSubject.apply)
  }

  property("recovery works for existing users") {
    forAll { (s: UserSubject, username: String, password: String) =>
      await {
        for {
          registerResult <- recoveryFlow.registerForRecovery(s)
          (f, token) = registerResult
          f2 <- f.recoverLogin(token, password) { _ =>
            ZIO.succeed((Some(username)))
          }
          authenticateResult <- f2.loginD.authenticate(Login(username, password))
          (_, subject) = authenticateResult
        } yield {
          assertEquals(subject.ref, s.ref)
        }
      }
    }
  }

  property("recovery returns InvalidCredential for non existing users") {
    forAll { (s: UserSubject, password: String) =>
      await {
        for {
          registerResult <- recoveryFlow.registerForRecovery(s)
          (f, token) = registerResult
          result <- f
            .recoverLogin(token, password) { _ =>
              ZIO.succeed(None)
            }
            .either
            .left
        } yield {
          assertEquals(result, AuthenticationError.InvalidCredential)
        }
      }
    }
  }

  property("successful recovery invalidates all existing credentials for a subject") {
    forAll { (s: UserSubject, username: String, password: String) =>
      await {
        for {
          registerResult <- recoveryFlow.registerForRecovery(s)
          (f, token1) = registerResult
          registerResult <- recoveryFlow.registerForRecovery(s)
          (f, token2) = registerResult
          f2 <- f.recoverLogin(token2, password)(_ => ZIO.succeed(Some(username)))
          authenticateResult <- f2.loginD.authenticate(Login(username, password))
          (_, subject) = authenticateResult
          recoverResult <- f
            .recoverLogin(token1, password)(_ => ZIO.succeed(Some(username)))
            .either
            .left
        } yield {
          assertEquals(recoverResult, AuthenticationError.InvalidCredential)
        }
      }
    }
  }

}
