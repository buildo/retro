---
id: token-based-recovery-flow
title: Token-based Recovery Flow
---

This flow allows to implement the password reset functionality of a web
application.

## Authentication domains

The flow defines and orchestrates two
[authentication domains](toctoc/concepts/functional-model.md):

- **`LoginDomain`**: an authentication domain whose credential is the `Login`
  data type.
- **`AccessTokenDomain`**: an authentication domain whose credential is the
  `AccessToken` data type.

The `Login` data type is defined by a username and a password, while the
`AccessToken` data type is defined by a value -- which is usually a randomly
generated string -- and an expiration date.

> 👉 Note: both the token-based authentication flow and the token-based recovery
> flow use the same kind of domain (`AccessTokenDomain`).
>
> However, you should normally have **two separate instances** for those
> domains, as their semantics and policies are likely to be different.
>
> For example, you usually want to limit the password recovery access token to
> the recovery functionality, and use an auth token for anything else.

## Functionalities

The flow is implemented by the `TokenBasedRecoveryFlow` class, which exposes
these functionalities:

- `registerForRecovery`: registers a `Subject` for recovery, by creating an
  `AccessToken` in the `AccessTokenDomain`. This is typically used when a user
  begins the password recovery flow.

- `recoverLogin`: resets the `Login` credentials for a `Subject`. The `Subject`
  is identified using an `AccessToken` normally issued by the
  `registerSubjectLogin` method above. You will also need to provide a function
  to recover the username for a given `Subject`, since the mapping between
  subjects and logins is outside the scope of toctoc.

## How to use it

You can create a `TokenBasedRecoveryFlow` by providing the two authentication
domains discussed above.

`toctoc` provides some specialized implementations of such domains combinations
of DBMS and data-access library. For this example we'll be using Postgres and
Slick, leveraging the `toctoc-slick-postgresql` module (see the
[installation docs](toctoc/installation.md) for instructions on how to add it as
a dependency).

```scala mdoc
// The generic flow
import io.buildo.toctoc.core.authentication.TokenBasedRecovery.TokenBasedRecoveryFlow

// The specialized AccessToken domain
import io.buildo.toctoc.slick.authentication.token.PostgreSqlSlickAccessTokenAuthenticationDomain

// The specialized Login domain
import io.buildo.toctoc.slick.authentication.login.PostgreSqlSlickLoginAuthenticationDomain

import cats.effect.Sync
import monix.catnap.FutureLift
import slick.jdbc.JdbcBackend.Database

import scala.concurrent.Future
import java.time.Duration

object PostgreSqlSlickTokenBasedRecoveryFlow {
  def create[F[_]: Sync: FutureLift[?[_], Future]](
    db: Database,
    tokenDuration: Duration
  ): TokenBasedRecoveryFlow[F] = TokenBasedRecoveryFlow.create[F](
    loginD = new PostgreSqlSlickLoginAuthenticationDomain[F](db),
    recoveryTokenD = new PostgreSqlSlickAccessTokenAuthenticationDomain[F](db),
    tokenDuration = tokenDuration,
  )
}
```

Now that we have it, we can integrate it in our app.

### Password recovery

For our example, we'll need a `UserService` which manages our users. Here's a
possible implementation:

```scala mdoc
import java.util.UUID

case class User(id: UUID, username: String, firstName: String, lastName: String)
case class UserCreate(firstName: String, lastName: String)

trait UserService[F[_]] {
  def create(user: UserCreate): F[UUID]
  def read(id: UUID): F[Option[User]]
  def readByUsername(username: String): F[Option[User]]
}
```

Now we can implement a controller that provides `beginPasswordRecovery` and
`completePasswordRecovery` functionalities:

```scala mdoc
import io.buildo.toctoc.core.authentication.TokenBasedRecovery.TokenBasedRecoveryFlow
import io.buildo.toctoc.core.authentication.TokenBasedAuthentication.AccessToken
import io.buildo.toctoc.core.authentication.TokenBasedAuthentication.UserSubject
import io.buildo.toctoc.core.authentication.AuthenticationError
import cats.effect.Sync
import cats.data.EitherT
import cats.implicits._
import java.util.UUID

trait PasswordRecoveryController[F[_]] {
  def beginPasswordRecovery(username: String): F[Either[AuthenticationError, AccessToken]]
  def completePasswordRecovery(
    recoveryToken: AccessToken,
    newPassword: String
  ): F[Either[AuthenticationError, Unit]]
}

object PasswordRecoveryController {
  def create[F[_]: Sync](
    recoveryFlow: TokenBasedRecoveryFlow[F],
    userService: UserService[F]
  ): PasswordRecoveryController[F] = new PasswordRecoveryController[F] {

    override def beginPasswordRecovery(username: String): F[Either[AuthenticationError, AccessToken]] =
      (for {
        user <- EitherT.fromOptionF(
          userService.readByUsername(username),
          AuthenticationError.InvalidCredential: AuthenticationError
        )
        result <-  EitherT(recoveryFlow.registerForRecovery(UserSubject(user.id.toString)))
        (_, recoveryToken) = result
      } yield recoveryToken).value

    override def completePasswordRecovery(
      recoveryToken: AccessToken,
      newPassword: String
    ): F[Either[AuthenticationError, Unit]] =
      recoveryFlow.recoverLogin(recoveryToken, newPassword) { subject =>
        userService.read(UUID.fromString(subject.ref)).map(_.map(_.username))
      }.map(_.void)

  }
}
```

## Sequence diagram

Here's a sequence diagram that shows the common interactions we've seen above

```scala mdoc:plantuml
autoactivate on

actor Subject
participant PasswordRecoveryController
participant AuthController

group password recovery
  Subject -> PasswordRecoveryController: beginPasswordRecovery(username)
  PasswordRecoveryController -> TokenBasedRecoveryFlow: registerForRecovery(username)
  return recoveryToken
  return recoveryToken (e.g. via email)

...

  Subject -> PasswordRecoveryController: completePasswordRecovery(recoveryToken, newPassword)
  PasswordRecoveryController -> TokenBasedRecoveryFlow: recoverLogin(recoveryToken, newPassword)
  return
  return
end
```
