package io.buildo.toctoc.authentication
import org.mindrot.jbcrypt.BCrypt
import scala.util.Random

trait HashModule {
  private val random = new Random()
  def randomString(n: Int, alphabet: String = "abcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+-{}[]\\|"): String =
    Stream.continually(random.nextInt(alphabet.size)).map(alphabet).take(n).mkString

  def hashPassword(p: String) =
    BCrypt.hashpw(p, generateSalt())

  def generateSalt() = BCrypt.gensalt()

  def checkPassword(candidate: String, hashed: String) =
    BCrypt.checkpw(candidate, hashed)
}

