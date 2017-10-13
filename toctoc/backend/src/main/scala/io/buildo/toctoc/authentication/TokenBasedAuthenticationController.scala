package io.buildo.toctoc.authentication

import scala.concurrent.Future

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

class TokenAuthenticationControllerImpl() extends TokenAuthenticationController {

  override def login(login: Login): Future[Either[AuthenticationError, TocTocToken]] = ???

  override def refresh(refreshToken: RefreshToken): Future[Either[AuthenticationError, TocTocToken]] = ???

  override def logout(token: Auth): Future[Either[AuthenticationError, Unit]] = ???

}
