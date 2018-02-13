// DO NOT EDIT MANUALLY - metarpheus-generated
import * as t from 'io-ts'

export interface AccessToken {
  value: string,
  expiresAt: string
}

export const AccessToken = t.interface({
  value: t.string,
  expiresAt: t.string
}, 'AccessToken')

export interface Login {
  username: string,
  password: string
}

export const Login = t.interface({
  username: t.string,
  password: t.string
}, 'Login')

export interface RefreshToken {
  value: string,
  expiresAt: string
}

export const RefreshToken = t.interface({
  value: t.string,
  expiresAt: t.string
}, 'RefreshToken')

export interface TocTocToken {
  accessToken: AccessToken,
  refreshToken: RefreshToken
}

export const TocTocToken = t.interface({
  accessToken: AccessToken,
  refreshToken: RefreshToken
}, 'TocTocToken')