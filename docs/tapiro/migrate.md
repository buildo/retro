---
id: migrate
title: Migration from Wiro
---

Tapiro is meant to deprecate [wiro](https://github.com/buildo/wiro).

Tapiro is based on the same concepts of wiro, the migration is pretty straightforward.

Here is a checklist of what you need to do:

1. Install the plugin (as described in the [guide](installation.md))
2. Configure your `build.sbt` (as described in the [guide](installation.md))
3. Add `Auth` type parameter to controllers
   `trait AccountController[F]` -> `trait AccountController[F[_], Auth]`
4. Modify controllers so that wiro `Auth` is replaced with `Auth` and move as last argument
   `def read(token: wiro.Auth, arg: Int)` -> `def read(arg: Int, token: Auth)`
5. Add `**/*Endpoints.scala linguist-generated` to repository's `.gitattributes` to automatically collapse tapiro generated code in GitHub diffs
6. Add required codecs
   This is a valid codec for wiro.Auth:

```scala mdoc
import sttp.tapir._
import sttp.tapir.Codec._

case class Auth(token: String) //should be imported as wiro.Auth instead

implicit val authCodec: PlainCodec[Auth] = Codec.string
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
