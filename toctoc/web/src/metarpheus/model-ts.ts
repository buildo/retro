
/* tslint:disable */


export interface AccessToken {
  value: string,
  expiresAt: string
}

export interface Login {
  username: string,
  password: string
}

export interface RefreshToken {
  value: string,
  expiresAt: string
}

export interface TocTocToken {
  accessToken: AccessToken,
  refreshToken: RefreshToken
}
