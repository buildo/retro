package io.buildo.toctoc
package quill
package authentication

import core.authentication.TokenBasedAuthentication.TokenBasedAuthenticationFlow
import token.QuillAccessTokenAuthenticationDomain
import login.QuillLoginAuthenticationDomain

import io.getquill.context.async.AsyncContext
import io.getquill.context.sql.idiom.SqlIdiom
import io.getquill.NamingStrategy
import com.github.mauricio.async.db.Connection

import scala.concurrent.ExecutionContext

import java.time.Duration

class QuillTokenBasedAuthenticationFlow[D <: SqlIdiom, N <: NamingStrategy, C <: Connection](
  ctx: AsyncContext[D, N, C],
  tokenDuration: Duration = Duration.ofDays(365)
)(implicit ec: ExecutionContext)
    extends TokenBasedAuthenticationFlow(
      loginD = new QuillLoginAuthenticationDomain(ctx),
      accessTokenD = new QuillAccessTokenAuthenticationDomain(ctx),
      tokenDuration = tokenDuration
    )
