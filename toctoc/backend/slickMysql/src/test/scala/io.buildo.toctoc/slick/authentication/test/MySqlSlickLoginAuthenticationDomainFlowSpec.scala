package io.buildo.toctoc
package slick
package authentication
package test

import core.authentication.TokenBasedAuthentication._
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import _root_.slick.jdbc.MySQLProfile.api._
import _root_.slick.jdbc.JdbcBackend.Database
import io.buildo.toctoc.slick.authentication.login.MySqlSlickLoginAuthenticationDomain
import io.buildo.toctoc.slick.authentication.token.MySqlSlickAccessTokenAuthenticationDomain

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration


class MySqlSlickLoginAuthenticationDomainFlowSpec extends FlatSpec
  with BeforeAndAfterEach
  with BeforeAndAfterAll
  with ScalaFutures
  with EitherValues
  with Matchers {

  val db = Database.forConfig("db")
  val loginAuthDomain = new MySqlSlickLoginAuthenticationDomain(db)(global)
  val accessTokenAuthDomain = new MySqlSlickAccessTokenAuthenticationDomain(db)(global)

  val loginTable = loginAuthDomain.loginTable
  val accessTokenTable = accessTokenAuthDomain.accessTokenTable
  val schema = loginTable.schema ++ accessTokenTable.schema

  val authFlow = new MySqlSlickTokenBasedAuthenticationFlow(db)

  override def beforeAll(): Unit = {
    db.run(schema.create).futureValue
  }

  override def afterEach(): Unit = {
    db.run(schema.truncate).futureValue
  }

  override def afterAll(): Unit = {
    println("Dropping schema")
    db.run(schema.drop).futureValue
    println("Ending tests")
    db.close()
  }

  val login = Login("username", "password")
  val login2 = Login("usernameoso", "password")
  val login3 = Login("usernameosone", "password")
  val subject = UserSubject("test")
  val subject2 = UserSubject("test2")

  "unregistered login credentials" should "not be accepted when exchanging for token" in {
    authFlow.exchangeForTokens(login).futureValue shouldBe 'left
  }

  "registered login credentials" should "be accepted when exchanging for token" in {
    authFlow.registerSubjectLogin(subject, login).futureValue shouldBe 'right
    authFlow.exchangeForTokens(login).futureValue shouldBe 'right
  }

  "token obtained by login" should "be validated" in {
    authFlow.registerSubjectLogin(subject, login).futureValue shouldBe 'right
    val token = authFlow.exchangeForTokens(login).futureValue.right.value
    authFlow.validateToken(token).futureValue.right.value shouldBe subject
  }

  "multiple login with same values" should "not be accepted in registration" in {
    authFlow.registerSubjectLogin(subject, login).futureValue shouldBe 'right
    authFlow.registerSubjectLogin(subject, login).futureValue shouldBe 'left
  }

  "multiple login with different values" should "be accepted in registration" in {
    authFlow.registerSubjectLogin(subject, login).futureValue shouldBe 'right
    authFlow.registerSubjectLogin(subject2, login2).futureValue shouldBe 'right
    val token2 = authFlow.exchangeForTokens(login2).futureValue.right.value
    authFlow.validateToken(token2).futureValue.right.value shouldBe subject2
  }

  "single token unregistration" should "unregister only the specific token" in {
    authFlow.registerSubjectLogin(subject, login).futureValue shouldBe 'right
    authFlow.registerSubjectLogin(subject2, login2).futureValue shouldBe 'right
    val token = authFlow.exchangeForTokens(login).futureValue.right.value
    val token2 = authFlow.exchangeForTokens(login2).futureValue.right.value
    authFlow.unregisterToken(token).futureValue
    authFlow.validateToken(token).futureValue shouldBe 'left
    authFlow.validateToken(token2).futureValue shouldBe 'right
  }

  "token unregistration" should "unregister all subject's tokens" in {
    authFlow.registerSubjectLogin(subject, login).futureValue shouldBe 'right
    authFlow.registerSubjectLogin(subject2, login2).futureValue shouldBe 'right
    val token = authFlow.exchangeForTokens(login).futureValue.right.value
    val token2 = authFlow.exchangeForTokens(login).futureValue.right.value
    val token3 = authFlow.exchangeForTokens(login2).futureValue.right.value
    authFlow.unregisterAllSubjectTokens(subject).futureValue
    authFlow.validateToken(token).futureValue shouldBe 'left
    authFlow.validateToken(token2).futureValue shouldBe 'left
    authFlow.validateToken(token3).futureValue shouldBe 'right
  }

  "single subject credentials unregistration" should "take effect" in {
    authFlow.registerSubjectLogin(subject, login).futureValue shouldBe 'right
    authFlow.unregisterLogin(login).futureValue
    authFlow.exchangeForTokens(login).futureValue shouldBe 'left
  }

  "subject credentials unregistration" should "take effect" in {
    authFlow.registerSubjectLogin(subject, login).futureValue shouldBe 'right
    authFlow.registerSubjectLogin(subject, login3).futureValue shouldBe 'right
    authFlow.unregisterAllSubjectLogins(subject).futureValue
    authFlow.exchangeForTokens(login).futureValue shouldBe 'left
    authFlow.exchangeForTokens(login3).futureValue shouldBe 'left
  }

}
