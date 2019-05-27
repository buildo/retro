package io.buildo.toctoc.circe

import io.buildo.toctoc.core.authentication.TokenBasedAuthentication._

import io.circe.Encoder
import io.circe.Decoder
import io.circe.generic.semiauto.deriveEncoder
import io.circe.generic.semiauto.deriveDecoder

trait CirceSupport {

  implicit val accessTokenEncoder: Encoder[AccessToken] = deriveEncoder
  implicit val accessTokenDecoder: Decoder[AccessToken] = deriveDecoder

  implicit val loginEncoder: Encoder[Login] = deriveEncoder
  implicit val loginDecoder: Decoder[Login] = deriveDecoder

}
