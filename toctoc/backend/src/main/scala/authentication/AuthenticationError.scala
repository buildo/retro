package authentication

import io.buildo.enumero.annotations.enum

@enum trait AuthenticationError {
  object InvalidAccessToken
  object ExpiredAccessToken
  object InvalidCredentials
}
