package io.buildo.toctoc.authentication
import java.security.MessageDigest
import scala.util.Random

trait HashModule {
  private lazy val random = new Random(new java.security.SecureRandom())
  private def sha1 = MessageDigest.getInstance("SHA-1")
  private def randomString(n: Int, alphabet: String = "abcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+-{}[]\\|"): String =
    Stream.continually(random.nextInt(alphabet.size)).map(alphabet).take(n).mkString

  def hashPassword(p: String, s: String) =
    sha1.digest((p + s).getBytes).map("%02x".format(_)).mkString

  def generateSalt() = randomString(64)
}

