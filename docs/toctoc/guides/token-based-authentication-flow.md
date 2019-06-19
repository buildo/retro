---
id: token-based-authentication-flow
title: Token-based Authentication Flow
---

This flow allows to implement the common signup, login and authentication
functionalities of a web application.

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

## Functionalities

The flow is implemented by the `TokenBasedAuthenticationFlow` class, which
exposes these functionalities:

- `registerSubjectLogin`: registers the `Login` of a `Subject` in the
  `LoginDomain`. This is typically used when signing up a new user.

- `exchangeForTokens`: exchanges a `Login` for an `AccessToken`. This is
  typically used when a user logs into the system providing username and
  password, and they receive an `AccessToken` that they will use for
  authenticating further requests.

- `validateToken`: checks whether an `AccessToken` is valid and returns the
  authenticated `Subject` if true.

- `unregisterToken`: invalidates an `AccessToken`. This is typically used when a
  user logs out from a device.

- `unregisterAllSubjectTokens`: invalidates all `AccessToken` of a `Subject`.
  This can be used to implement security features, such as logging out from all
  active sessions in case the password is suspected to be compromised or upon a
  change password request.

- `unregisterLogin`: invalidates a `Login`. This can be used in case of a change
  password request in an app that wants to guarantee a single login at the time
  per user.

- `unregisterAllSubjectLogins`: invalidates all `Login` of a `Subject`. This is
  typically used when removing a user from the system.

## How to use it

You can create a `TokenBasedAuthenticationFlow` by providing the two
authentication domains discussed above.

`toctoc` provides some specialized implementations of such domains combinations
of DBMS and data-access library. For this example we'll be using Postgres and
Slick, leveraging the `toctoc-slick-postgresql` module (see the
[installation docs](toctoc/installation.md) for instructions on how to add it as
a dependency).

```scala mdoc
// The generic flow
import io.buildo.toctoc.core.authentication.TokenBasedAuthentication.TokenBasedAuthenticationFlow

// The specialized AccessToken domain
import io.buildo.toctoc.slick.authentication.token.PostgreSqlSlickAccessTokenAuthenticationDomain

// The specialized Login domain
import io.buildo.toctoc.slick.authentication.login.PostgreSqlSlickLoginAuthenticationDomain

import cats.effect.Sync
import monix.catnap.FutureLift
import slick.jdbc.JdbcBackend.Database

import scala.concurrent.Future
import java.time.Duration

object PostgreSqlSlickTokenBasedAuthenticationFlow {
  def create[F[_]: Sync: FutureLift[?[_], Future]](
    db: Database,
    tokenDuration: Duration
  ): TokenBasedAuthenticationFlow[F] = new TokenBasedAuthenticationFlow[F](
    loginD = new PostgreSqlSlickLoginAuthenticationDomain[F](db),
    accessTokenD = new PostgreSqlSlickAccessTokenAuthenticationDomain[F](db),
    tokenDuration = tokenDuration,
  )
}
```

Now that we have it, we can integrate it in our app.

### Login and Logout

Here's a possible implementation of a controller that provides `login` and
`logout` functionalities:

```scala mdoc
import io.buildo.toctoc.core.authentication.TokenBasedAuthentication.TokenBasedAuthenticationFlow
import io.buildo.toctoc.core.authentication.TokenBasedAuthentication.AccessToken
import io.buildo.toctoc.core.authentication.TokenBasedAuthentication.Login
import io.buildo.toctoc.core.authentication.AuthenticationError
import cats.effect.Sync

trait AuthenticationController[F[_]] {
  def login(credentials: Login): F[Either[AuthenticationError, AccessToken]]
  def logout(accessToken: AccessToken): F[Either[AuthenticationError, Unit]]
}

object AuthenticationController {
  def create[F[_]: Sync](authFlow: TokenBasedAuthenticationFlow[F]): AuthenticationController[F] = new AuthenticationController[F] {

    override def login(credentials: Login):  F[Either[AuthenticationError, AccessToken]] =
      authFlow.exchangeForTokens(credentials)

    override def logout(accessToken: AccessToken): F[Either[AuthenticationError, Unit]] =
      authFlow.unregisterToken(accessToken)

  }
}
```

### Signup

For implementing a signup we need to know how to create users in our app.

Let's introduce a `UserService` which manages our users:

```scala mdoc
import java.util.UUID

case class User(id: UUID, firstName: String, lastName: String)
case class UserCreate(firstName: String, lastName: String)

trait UserService[F[_]] {
  def create(user: UserCreate): F[UUID]
  def read(id: UUID): F[Option[User]]
}
```

We can now implement a `UserController` which uses the service and the
authentication flow to provide the `signup` functionality:

```scala mdoc
import io.buildo.toctoc.core.authentication.TokenBasedAuthentication.TokenBasedAuthenticationFlow
import io.buildo.toctoc.core.authentication.TokenBasedAuthentication.Login
import io.buildo.toctoc.core.authentication.TokenBasedAuthentication.UserSubject
import io.buildo.toctoc.core.authentication.AuthenticationError
import cats.effect.Sync
import cats.implicits._

trait UserController[F[_]] {
  def signup(credentials: Login, user: UserCreate): F[Either[AuthenticationError, Unit]]
}

object UserController {
  def create[F[_]: Sync](userService: UserService[F], authFlow: TokenBasedAuthenticationFlow[F]): UserController[F] = new UserController[F] {
    override def signup(credentials: Login, user: UserCreate): F[Either[AuthenticationError, Unit]] =
    for {
      // create the user
      userId <- userService.create(user)
      // create the login
      res <- authFlow.registerSubjectLogin(UserSubject(userId.toString), credentials)
    } yield res

  }
}
```

## Sequence diagram

Here's a sequence diagram that shows the common interactions we've seen above

```scala mdoc:plantuml
autoactivate on

actor Subject
participant UserController
participant AuthController

group signup
  Subject -> UserController: signup(credentials, user)
  UserController -> UserService: create(user)
  return userId
  UserController -> TokenBasedAuthenticationFlow: registerSubjectLogin(userId, credentials)
  return
  return userId
end

...

group login
  Subject -> AuthController: login(credentials)
  AuthController -> TokenBasedAuthenticationFlow: exchangeForTokens(credentials)
  return accessToken
  return accessToken
end

...

group logout
  Subject -> AuthController: logout(accessToken)
  AuthController -> TokenBasedAuthenticationFlow: unregisterToken(accessToken)
  return
  return
end
```
