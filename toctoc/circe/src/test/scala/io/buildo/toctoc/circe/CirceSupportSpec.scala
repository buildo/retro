package io.buildo.toctoc.circe

import io.buildo.toctoc.core.authentication.TokenBasedAuthentication._

import org.scalatest._
import org.scalatest.prop._
import org.scalacheck.magnolia._
import io.circe.syntax._
import io.circe.Json
import org.scalacheck.Arbitrary
import org.scalacheck.Gen
import java.time.Duration
import java.time.temporal.ChronoUnit

final class CirceSupportSpec extends PropSpec with PropertyChecks {

  implicit val arbAccessToken: Arbitrary[AccessToken] = Arbitrary {
    for {
      duration <- Gen.finiteDuration
      value <- Gen.alphaNumStr
    } yield AccessToken.generate(value, Duration.of(duration.toMillis, ChronoUnit.MILLIS))
  }

  property("encodes AccessToken correctly") {
    forAll { (token: AccessToken) =>
      assertResult(
        Json.obj(
          "value" -> token.value.asJson,
          "expiresAt" -> token.expiresAt.asJson,
        ),
      )(token.asJson)
    }
  }

  property("decodes AccessToken correctly") {
    forAll { (token: AccessToken) =>
      val json = Json.obj(
        "value" -> token.value.asJson,
        "expiresAt" -> token.expiresAt.asJson,
      )
      assertResult(json.as[AccessToken].right.get)(token)
    }
  }

  property("encodes Login correctly") {
    forAll { (login: Login) =>
      assertResult(
        Json.obj(
          "username" -> login.username.asJson,
          "password" -> login.password.asJson,
        ),
      )(login.asJson)
    }
  }

  property("decodes Login correctly") {
    forAll { (login: Login) =>
      val json = Json.obj(
        "username" -> login.username.asJson,
        "password" -> login.password.asJson,
      )
      assertResult(json.as[Login].right.get)(login)
    }
  }

}
