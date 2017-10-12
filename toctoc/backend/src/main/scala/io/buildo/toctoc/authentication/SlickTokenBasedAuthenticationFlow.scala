package io.buildo.toctoc.authentication

import slick.jdbc.JdbcBackend.Database

import io.buildo.toctoc.authentication.TokenBasedAuthentication.TokenBasedAuthenticationFlow
import io.buildo.toctoc.authentication.token.SlickAccessTokenAuthenticationDomain
import io.buildo.toctoc.authentication.login.SlickLoginAuthenticationDomain

import slick.jdbc.JdbcBackend.Database

import java.time.Duration

class SlickTokenBasedAuthenticationFlow(db: Database, tokenDuration: Duration = Duration.ofDays(365))
  extends TokenBasedAuthenticationFlow(
    loginD = new SlickLoginAuthenticationDomain(db),
    accessTokenD = new SlickAccessTokenAuthenticationDomain(db),
    tokenDuration = tokenDuration
  )
