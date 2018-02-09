package io.buildo.toctoc
package wiro
package authentication

import core.authentication.AuthenticationError
import core.authentication.TokenBasedAuthentication._

import _root_.wiro.annotation._
import _root_.wiro.Auth

import cats.data.EitherT
import cats.instances.future._

import scala.concurrent.{ Future, ExecutionContext }

import java.time.Instant

@path("toctoc")
trait TokenAuthenticationController {

  @command
  def login(login: Login): Future[Either[AuthenticationError, TocTocToken]]

  @command
  def refresh(refreshToken: RefreshToken): Future[Either[AuthenticationError, TocTocToken]]

  @command
  def logout(token: Auth): Future[Either[AuthenticationError, Unit]]

}

class TokenAuthenticationControllerImpl(
  flow: TokenBasedAuthenticationFlow
)(implicit
  ec: ExecutionContext
) extends TokenAuthenticationController {

  private def mockedRefreshToken(t: AccessToken): RefreshToken =
    RefreshToken(t.value, t.expiresAt)

  implicit class AuthOps(a: Auth) {
    def toAccessToken = AccessToken(a.token, Instant.now)
  }

  override def login(login: Login): Future[Either[AuthenticationError, TocTocToken]] =
    (for {
      accessToken <- EitherT(flow.exchangeForTokens(login))
      refreshToken = mockedRefreshToken(accessToken)
      tocTocToken = TocTocToken(accessToken, refreshToken)
    } yield tocTocToken).value

  override def refresh(refreshToken: RefreshToken): Future[Either[AuthenticationError, TocTocToken]] =
    Future.successful(Left(AuthenticationError.InvalidCredential))

  override def logout(token: Auth): Future[Either[AuthenticationError, Unit]] =
    flow.unregisterToken(token.toAccessToken)

}
