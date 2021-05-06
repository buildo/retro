package wiro

import autowire._

import io.circe.generic.auto._

import wiro.client.akkaHttp._
import wiro.client.akkaHttp.FailSupport._
import wiro.TestController.{userRouter, User, UserController}

class WiroSpec extends munit.FunSuite with RPCRouteTest with ClientDerivationModule {
  private[this] val rpcClient = new RPCClientTest(
    deriveClientContext[UserController],
    userRouter.buildRoute,
  ).apply[UserController]

  test("A GET request when it's right should return 200 and content") {
    rpcClient.read(1).call().map { user =>
      assertEquals(user, Right(User(1, "read")))
    }
  }

}
