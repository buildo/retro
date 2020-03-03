---
id: migrate
title: Migration from Wiro
---

Tapiro is meant to deprecate [wiro](https://github.com/buildo/wiro).

Tapiro is based on the same concepts of wiro, the migration is pretty straightforward.

Here is a checklist of what you need to do:
1. Install the plugin (as described in the [guide](installation.md))
2. Configure your `build.sbt` (as described in the [guide](installation.md))
3. Add `AuthToken` type parameter to controllers
    `trait AccountController` -> `trait AccountController[AuthToken]`
4. Modify controllers so that wiro `Auth` is replaced with AuthToken and move as last argument
    `def read(token: Auth, arg: Int)` -> `def read(arg: Int, token: AuthToken)`
5. Add **/*Endpoints.scala linguist-generated to repository's .gitattributes to hide tapiro generated code from github diff's
6. Add required codecs
This is a valid codec for wiro.Auth:

```scala mdoc
import sttp.tapir._
import sttp.tapir.Codec._

case class Auth(token: String) //should be imported as wiro.Auth instead

implicit val authCodec: PlainCodec[Auth] = Codec.stringPlainCodecUtf8
  .mapDecode(decodeAuth)(encodeAuth)

def decodeAuth(s: String): DecodeResult[Auth] = {
  val TokenPattern = "Token token=(.+)".r
  s match {
    case TokenPattern(token) => DecodeResult.Value(Auth(token))
    case _                   => DecodeResult.Error(s, new Exception("token not found"))
  }
}

def encodeAuth(auth: Auth): String = auth.token
```
7. Run `sbt tapiro`

Using `Server.AkkaHttp` the resulting routes can be added to wiro as custom routes.