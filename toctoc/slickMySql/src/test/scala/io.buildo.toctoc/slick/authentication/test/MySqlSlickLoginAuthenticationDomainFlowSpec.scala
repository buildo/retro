package io.buildo.toctoc
package slick
package authentication
package test

import java.time.Duration

import zio.ZIO
import _root_.slick.jdbc.MySQLProfile.api._
import _root_.slick.jdbc.JdbcBackend.Database

import core.authentication.TokenBasedAuthentication._
import core.authentication.test.ZIOSupport
import io.buildo.toctoc.slick.authentication.login.MySqlSlickLoginAuthenticationDomain
import io.buildo.toctoc.slick.authentication.token.MySqlSlickAccessTokenAuthenticationDomain

class MySqlSlickLoginAuthenticationDomainFlowSpec extends munit.FunSuite with ZIOSupport {

  val loginTableName = "login"
  val tokenTableName = "token"

  val db = Database.forConfig("db")
  val loginAuthDomain = new MySqlSlickLoginAuthenticationDomain(db, loginTableName)
  val accessTokenAuthDomain = new MySqlSlickAccessTokenAuthenticationDomain(db, tokenTableName)

  val loginTable = loginAuthDomain.loginTable
  val accessTokenTable = accessTokenAuthDomain.accessTokenTable
  val schema = loginTable.schema ++ accessTokenTable.schema

  val authFlow =
    new TokenBasedAuthenticationFlow(loginAuthDomain, accessTokenAuthDomain, Duration.ofDays(1))

  override def beforeAll(): Unit = await(ZIO.fromFuture(_ => db.run(schema.create)).unit)

  override def afterEach(context: AfterEach): Unit = await(
    ZIO.fromFuture(_ => db.run(schema.truncate)).unit,
  )

  override def afterAll(): Unit = await {
    for {
      _ <- ZIO.fromFuture(_ => db.run(schema.drop))
      _ <- ZIO.attempt(db.close())
    } yield ()
  }

  val login = Login("username", "password")
  val login2 = Login("usernameoso", "password")
  val login3 = Login("usernameosone", "password")
  val subject = UserSubject("test")
  val subject2 = UserSubject("test2")

  test("unregistered login credentials should not be accepted when exchanging for token") {
    for {
      result <- authFlow.exchangeForTokens(login).either
    } yield {
      assert(result.isLeft)
    }
  }

  test("registered login credentials should be accepted when exchanging for token") {
    for {
      register <- authFlow.registerSubjectLogin(subject, login).either
      token <- authFlow.exchangeForTokens(login).either
    } yield {
      assert(register.isRight)
      assert(token.isRight)
    }
  }

  test("token obtained by login should be validated") {
    for {
      result <- authFlow.registerSubjectLogin(subject, login).either
    } yield {
      assert(result.isRight)
    }
    for {
      token <- authFlow.exchangeForTokens(login)
      result <- authFlow.validateToken(token)
    } yield {
      assertEquals(result, subject)
    }
  }

  test("multiple login with same values should not be accepted in registration") {
    for {
      register <- authFlow.registerSubjectLogin(subject, login).either
      register2 <- authFlow.registerSubjectLogin(subject, login).either
    } yield {
      assert(register.isRight)
      assert(register2.isLeft)
    }
  }

  test("multiple login with different values should be accepted in registration") {
    for {
      register <- authFlow.registerSubjectLogin(subject, login).either
      register2 <- authFlow.registerSubjectLogin(subject2, login2).either
    } yield {
      assert(register.isRight)
      assert(register2.isRight)
    }
    for {
      token2 <- authFlow.exchangeForTokens(login2)
      result <- authFlow.validateToken(token2)
    } yield {
      assertEquals(result, subject2)
    }
  }

  test("single token unregistration should unregister only the specific token") {
    for {
      registerLogin <- authFlow.registerSubjectLogin(subject, login).either
      registerLogin2 <- authFlow.registerSubjectLogin(subject2, login2).either
      token <- authFlow.exchangeForTokens(login)
      token2 <- authFlow.exchangeForTokens(login2)
      _ <- authFlow.unregisterToken(token)
      validateToken <- authFlow.validateToken(token).either
      validateToken2 <- authFlow.validateToken(token2).either
    } yield {
      assert(registerLogin.isRight)
      assert(registerLogin2.isRight)
      assert(validateToken.isLeft)
      assert(validateToken2.isRight)
    }
  }

  test("token unregistration should unregister all subject's tokens") {
    for {
      register <- authFlow.registerSubjectLogin(subject, login).either
      register2 <- authFlow.registerSubjectLogin(subject2, login2).either
      token <- authFlow.exchangeForTokens(login)
      token2 <- authFlow.exchangeForTokens(login)
      token3 <- authFlow.exchangeForTokens(login2)
      _ <- authFlow.unregisterAllSubjectTokens(subject)
      validateToken <- authFlow.validateToken(token).either
      validateToken2 <- authFlow.validateToken(token2).either
      validateToken3 <- authFlow.validateToken(token3).either
    } yield {
      assert(register.isRight)
      assert(register2.isRight)
      assert(validateToken.isLeft)
      assert(validateToken2.isLeft)
      assert(validateToken3.isRight)
    }
  }

  test("single subject credentials unregistration should take effect") {
    for {
      register <- authFlow.registerSubjectLogin(subject, login).either
      _ <- authFlow.unregisterLogin(login)
      result <- authFlow.exchangeForTokens(login).either
    } yield {
      assert(register.isRight)
      assert(result.isLeft)
    }
  }

  test("subject credentials unregistration should take effect") {
    for {
      register <- authFlow.registerSubjectLogin(subject, login).either
      register3 <- authFlow.registerSubjectLogin(subject, login3).either
      _ <- authFlow.unregisterAllSubjectLogins(subject)
      result <- authFlow.exchangeForTokens(login).either
      result2 <- authFlow.exchangeForTokens(login3).either
    } yield {
      assert(register.isRight)
      assert(register3.isRight)
      assert(result.isLeft)
      assert(result2.isLeft)
    }
  }
}
