package wiro

import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.MethodRejection
import akka.util.ByteString
import de.heikoseeberger.akkahttpcirce.ErrorAccumulatingCirceSupport._
import io.circe.generic.auto._
import wiro.TestController._

class WiroSpec extends munit.FunSuite with MUnitRouteTest {

  private[this] def jsonEntity(data: ByteString) = HttpEntity(
    contentType = MediaTypes.`application/json`,
    data = data,
  )

  test("A POST request when it's right should return 200 and content") {
    val data = ByteString(s"""
                             |{
                             |    "id": 1,
                             |    "user": {
                             |        "id": 1,
                             |        "username": "foo"
                             |    }
                             |}
          """.stripMargin)

    Post("/user/update", jsonEntity(data)) ~> userRouter.buildRoute ~> check {
      assertEquals(status, OK)
      assertEquals(responseAs[Ok], Ok("update"))
    }
  }

  test(
    "A POST request when it points to route that includes the name of another route should invoke the correct path",
  ) {
    val data = ByteString(s"""
                             |{
                             |    "id": 1,
                             |    "user": {
                             |        "id": 1,
                             |        "username": "foo"
                             |    }
                             |}
          """.stripMargin)

    Post("/user/updateCommand", jsonEntity(data)) ~> userRouter.buildRoute ~> check {
      assertEquals(status, OK)
      assertEquals(responseAs[Ok], Ok("updateCommand"))
    }
  }

  test("A POST request when it's a left should return provided error") {
    val data = ByteString(s"""
                             |{
                             |    "id": 2,
                             |    "user": {
                             |        "id": 2,
                             |        "username": "foo"
                             |    }
                             |}
          """.stripMargin)

    Post("/user/update", jsonEntity(data)) ~> userRouter.buildRoute ~> check {
      assertEquals(status, NotFound)
    }
  }

  test("A POST request when operation is overridden should overridden route should be used") {
    val data = ByteString(s"""
                             |{
                             |    "id": 2,
                             |    "user": {
                             |        "id": 2,
                             |        "username": "foo"
                             |    }
                             |}
          """.stripMargin)

    Post("/user/insert", jsonEntity(data)) ~> userRouter.buildRoute ~> check {
      assertEquals(status, OK)
    }
  }

  test("A POST request when it has an unsuitable body should return 422") {
    val data = ByteString(s"""
                             |{
                             |    "id": "foo",
                             |    "user": {
                             |        "username": "foo",
                             |        "id": 1
                             |    }
                             |}
          """.stripMargin)

    Post("/user/update", jsonEntity(data)) ~> userRouter.buildRoute ~> check {
      assertEquals(status, UnprocessableEntity)
    }
  }

  test("A POST request when it has an unsuitable body in nested resource should return 422") {
    val data = ByteString(s"""
                             |{
                             |    "id": 1,
                             |    "user": {
                             |        "username": "foo"
                             |    }
                             |}
          """.stripMargin)

    Post("/user/update", jsonEntity(data)) ~> userRouter.buildRoute ~> check {
      assertEquals(status, UnprocessableEntity)
    }
  }

  test("A POST request when HTTP method is wrong should return method is missing when GET") {
    Get("/user/update?id=1") ~> userRouter.buildRoute ~> check {
      assertEquals(rejections, List(MethodRejection(POST)))
    }
  }

  test("A POST request when HTTP method is wrong should return method is missing when DELETE") {
    Delete("/user/update?id=1") ~> userRouter.buildRoute ~> check {
      assertEquals(rejections, List(MethodRejection(POST)))
    }
  }

  test("A POST request when HTTP method is wrong should return method is missing when PUT") {
    Put("/user/update?id=1") ~> userRouter.buildRoute ~> check {
      assertEquals(rejections, List(MethodRejection(POST)))
    }
  }

  test("A POST request when operation doesn't exist should return 405") {
    val data = ByteString(s"""
                             |{
                             |    "id": 1,
                             |    "user": {
                             |        "username": "foo"
                             |    }
                             |}
          """.stripMargin)

    Post("/user/updat", jsonEntity(data)) ~> userRouter.buildRoute ~> check {
      assertEquals(rejections, Nil)
    }
  }

  test("A GET request when it's right should return 200 and content") {
    Get("/user/read?id=1") ~> userRouter.buildRoute ~> check {
      assertEquals(status, OK)
      assertEquals(responseAs[User], (User(1, "read")))
    }
  }

  test(
    "A GET request when it contains query params with integer as only argument should return 200 and content",
  ) {
    Get("/user/readString?id=1") ~> userRouter.buildRoute ~> check {
      assertEquals(status, OK)
      assertEquals(responseAs[User], (User(1, "read")))
    }
  }

  test("A GET request when it's authenticated should block user without proper token") {
    Get("/user/nobodyCannaCrossIt") ~> userRouter.buildRoute ~> check {
      assertEquals(status, StatusCodes.Unauthorized)
    }
  }

  test("A GET request when it's authenticated should not block user having proper token") {
    Get("/user/nobodyCannaCrossIt") ~> addHeader(
      "Authorization",
      "Token token=bus",
    ) ~> userRouter.buildRoute ~> check {
      assertEquals(status, OK)
      assertEquals(responseAs[Ok], Ok("di bus can swim"))
    }
  }

  test("A GET request when it has headers should allow the user to read them") {
    val headerName = "header"
    val headerContent = "content"
    Get("/user/inLoveWithMyHeaders") ~> addHeader(
      headerName,
      headerContent,
    ) ~> userRouter.buildRoute ~> check {
      assertEquals(status, OK)
      assert(
        clue(responseAs[OperationParameters].parameters).exists(_ == headerName -> headerContent),
      )
    }
  }

  test(
    "A GET request when it points to route that includes the name of another route should invoke the correct path",
  ) {
    Get("/user/readQuery?id=1") ~> userRouter.buildRoute ~> check {
      assertEquals(status, OK)
      assertEquals(responseAs[User], (User(1, "readQuery")))
    }
  }

  test("A GET request when it's left should return provided error") {
    Get("/user/read?id=2") ~> userRouter.buildRoute ~> check {
      assertEquals(status, NotFound)
    }
  }

  test("A GET request when operation is overridden it should use overridden route should be used") {
    Get("/user/number") ~> userRouter.buildRoute ~> check {
      assertEquals(status, OK)
    }
  }

  test("A GET request when the HTTP method is wrong should return method is missing when POST") {
    val data = ByteString(s"""
                             |{
                             |    "id": 1
                             |}
          """.stripMargin)

    Post("/user/read", jsonEntity(data)) ~> userRouter.buildRoute ~> check {
      assertEquals(rejections, List(MethodRejection(GET)))
    }
  }

  test("A GET request when the HTTP method is wrong should return method is missing when DELETE") {
    Delete("/user/read") ~> userRouter.buildRoute ~> check {
      assertEquals(rejections, List(MethodRejection(GET)))
    }
  }

  test("A GET request when the HTTP method is wrong should return method is missing when PUT") {
    Put("/user/read") ~> userRouter.buildRoute ~> check {
      assertEquals(rejections, List(MethodRejection(GET)))
    }
  }

  test("A GET request when the operation doesn't exist should be rejected") {
    Get("/user/rea") ~> userRouter.buildRoute ~> check {
      assertEquals(rejections, Nil)
    }
  }
}
