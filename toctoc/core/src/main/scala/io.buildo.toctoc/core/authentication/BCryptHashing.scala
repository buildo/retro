package io.buildo.toctoc
package core
package authentication

import org.mindrot.jbcrypt.BCrypt

import scala.util.Random

import java.security.SecureRandom

trait BCryptHashing {
  private val random = new Random(new SecureRandom)
  private val defaultAlphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
  def randomString(n: Int, alphabet: String = defaultAlphabet): String =
    Stream.continually(random.nextInt(alphabet.size)).map(alphabet).take(n).mkString

  def hashPassword(p: String): String =
    BCrypt.hashpw(p, generateSalt())

  def generateSalt(): String = BCrypt.gensalt()

  def checkPassword(candidate: String, hashed: String): Boolean =
    BCrypt.checkpw(candidate, hashed)
}

