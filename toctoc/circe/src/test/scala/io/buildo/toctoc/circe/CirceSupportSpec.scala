package io.buildo.toctoc.circe

import io.buildo.toctoc.core.authentication.TokenBasedAuthentication._

import org.scalacheck.Prop.forAll
import io.circe.syntax._
import io.circe.Json
import org.scalacheck.Arbitrary
import org.scalacheck.Gen
import java.time.Duration
import java.time.temporal.ChronoUnit

final class CirceSupportSpec extends munit.ScalaCheckSuite {

  implicit val arbAccessToken: Arbitrary[AccessToken] = Arbitrary {
    for {
      duration <- Gen.finiteDuration
      value <- Gen.alphaNumStr
    } yield AccessToken.generate(value, Duration.of(duration.toMillis, ChronoUnit.MILLIS))
  }

  property("encodes AccessToken correctly") {
    forAll { (token: AccessToken) =>
      assertEquals(
        token.asJson,
        Json.obj(
          "value" -> token.value.asJson,
          "expiresAt" -> token.expiresAt.asJson,
        ),
      )
    }
  }

  property("decodes AccessToken correctly") {
    forAll { (token: AccessToken) =>
      val json = Json.obj(
        "value" -> token.value.asJson,
        "expiresAt" -> token.expiresAt.asJson,
      )
      assertEquals(token, json.as[AccessToken].right.get)
    }
  }

  property("encodes Login correctly") {
    forAll { (login: Login) =>
      assertEquals(
        login.asJson,
        Json.obj(
          "username" -> login.username.asJson,
          "password" -> login.password.asJson,
        ),
      )
    }
  }

  property("decodes Login correctly") {
    forAll { (login: Login) =>
      val json = Json.obj(
        "username" -> login.username.asJson,
        "password" -> login.password.asJson,
      )
      assertEquals(login, json.as[Login].right.get)
    }
  }

}
