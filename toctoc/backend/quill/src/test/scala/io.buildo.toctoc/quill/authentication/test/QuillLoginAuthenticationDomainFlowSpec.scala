package io.buildo.toctoc
package quill
package authentication
package test

import core.authentication.TokenBasedAuthentication._
import login.QuillLoginAuthenticationDomain
import token.QuillAccessTokenAuthenticationDomain

import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import io.getquill._
import io.getquill.{CamelCase, PostgresAsyncContext}
import org.flywaydb.core.Flyway
import com.typesafe.config.ConfigFactory

import scala.concurrent.ExecutionContext.Implicits.global

class QuillLoginAuthenticationDomainFlowSpec
    extends FlatSpec
    with BeforeAndAfterEach
    with BeforeAndAfterAll
    with ScalaFutures
    with EitherValues
    with Matchers {

  val ctx = new PostgresAsyncContext(CamelCase, "db")
  val loginAuthDomain = new QuillLoginAuthenticationDomain(ctx)(global)
  val accessTokenAuthDomain =
    new QuillAccessTokenAuthenticationDomain(ctx)(global)

  val authFlow = new QuillTokenBasedAuthenticationFlow(ctx)

  val flyway = {
    val flyway = new Flyway()
    val config = ConfigFactory.load().getConfig("db")
    val host = config.getString("host")
    val port = config.getString("port")
    val user = config.getString("user")
    val password = config.getString("password")
    val database = config.getString("database")
    flyway.setDataSource(s"jdbc:postgresql://$host:$port/$database", user, password)
    flyway
  }

  override def beforeAll() = {
    flyway.clean()
    flyway.migrate()
    ()
  }

  override def afterEach() = {
    import ctx._
    ctx.run(quote(querySchema("LoginAuthDomain").delete))
    ctx.run(quote(querySchema("AccessTokenAuthDomain").delete))
    ()
  }

  override def afterAll() = {
    flyway.clean()
    ctx.close()
  }

  import org.scalatest.time._
  implicit val defaultPatience =
    PatienceConfig(timeout = Span(2, Seconds), interval = Span(5, Millis))

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
