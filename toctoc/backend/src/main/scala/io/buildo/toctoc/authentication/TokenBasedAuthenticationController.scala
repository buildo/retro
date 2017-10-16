package io.buildo.toctoc.authentication

import java.time.Instant
import scala.concurrent.{ Future, ExecutionContext }

import cats.data.EitherT
import cats.instances.future._
import wiro.annotation._
import wiro.Auth

import io.buildo.toctoc.authentication.TokenBasedAuthentication._

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
