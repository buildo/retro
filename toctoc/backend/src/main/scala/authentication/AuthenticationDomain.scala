package authentication

import scala.concurrent.Future

trait AuthenticationDomain[C <: Credential] {
  def authenticate(c: C): Future[Either[AuthenticationError, (AuthenticationDomain[C], Subject)]]
  def register(s: Subject, c: C): Future[Either[AuthenticationError, AuthenticationDomain[C]]]
  def unregister(s: Subject): Future[Either[AuthenticationError, AuthenticationDomain[C]]]
}
