package io.buildo.toctoc
package slick
package authentication
package test

import core.authentication.TokenBasedAuthentication._
import login.SlickLoginAuthenticationDomain
import token.SlickAccessTokenAuthenticationDomain

import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import _root_.slick.jdbc.PostgresProfile.api._
import _root_.slick.jdbc.JdbcBackend.Database

import scala.concurrent.ExecutionContext.Implicits.global

class SlickLoginAuthenticationDomainFlowSpec extends FlatSpec
  with BeforeAndAfterAll
  with ScalaFutures
  with EitherValues
  with Matchers {

  val db = Database.forConfig("db")
  val loginAuthDomain = new SlickLoginAuthenticationDomain(db)(global)
  val accessTokenAuthDomain = new SlickAccessTokenAuthenticationDomain(db)(global)

  val loginTable = loginAuthDomain.loginTable
  val accessTokenTable = accessTokenAuthDomain.accessTokenTable
  val schema = loginTable.schema ++ accessTokenTable.schema

  val authFlow = new SlickTokenBasedAuthenticationFlow(db)

  override def beforeAll() = {
    db.run(schema.create).futureValue
  }

  override def afterAll() = {
    db.run(schema.drop).futureValue
    db.close()
  }

  val login = Login("username", "password")
  val login2 = Login("usernameoso", "password")
  val login3 = Login("usernameosone", "password")
  val subject = UserSubject("test")
  val subject2 = UserSubject("test2")

  "unregistered login credentials" should "not be accepted when exchanging for token" in {
    authFlow.exchangeForTokens(login).futureValue.left.get
  }

  "registered login credentials" should "be accepted when exchanging for token" in {
    authFlow.registerSubjectLogin(subject, login).futureValue.right.get
    authFlow.exchangeForTokens(login).futureValue.right.get
  }

  "token obtained by login" should "be validated" in {
    val token = authFlow.exchangeForTokens(login).futureValue.right.get
    authFlow.validateToken(token).futureValue.right.value should be (subject)
  }

  "multiple login with same values" should "not be accepted in registration" in {
    authFlow.registerSubjectLogin(subject, login).futureValue.left.get
  }

  "multiple login with different values" should "be accepted in registration" in {
    authFlow.registerSubjectLogin(subject2, login2).futureValue.right.get
    val token2 = authFlow.exchangeForTokens(login2).futureValue.right.get
    authFlow.validateToken(token2).futureValue.right.value should be (subject2)
  }

  "single token unregistration" should "unregister only the specific token" in {
    val token = authFlow.exchangeForTokens(login2).futureValue.right.get
    val token2 = authFlow.exchangeForTokens(login2).futureValue.right.get
    authFlow.unregisterToken(token).futureValue
    authFlow.validateToken(token).futureValue.left.get
    authFlow.validateToken(token2).futureValue.right.get
  }

  "token unregistration" should "unregister all subject's tokens" in {
    val token = authFlow.exchangeForTokens(login).futureValue.right.get
    val token2 = authFlow.exchangeForTokens(login).futureValue.right.get
    val token3 = authFlow.exchangeForTokens(login2).futureValue.right.get
    authFlow.unregisterAllSubjectTokens(subject).futureValue
    authFlow.validateToken(token).futureValue.left.get
    authFlow.validateToken(token2).futureValue.left.get
    authFlow.validateToken(token3).futureValue.right.get
  }

  "single subject credentials unregistration" should "take effect" in  {
    authFlow.unregisterLogin(login).futureValue
    authFlow.exchangeForTokens(login).futureValue.left.get
  }

  "subject credentials unregistration" should "take effect" in  {
    authFlow.registerSubjectLogin(subject, login).futureValue.right.get
    authFlow.registerSubjectLogin(subject, login3).futureValue.right.get
    authFlow.unregisterAllSubjectLogins(subject).futureValue
    authFlow.exchangeForTokens(login).futureValue.left.get
    authFlow.exchangeForTokens(login3).futureValue.left.get
  }

}
