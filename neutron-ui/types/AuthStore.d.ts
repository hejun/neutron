export interface AuthStore {
  authentication: Authentication | null
  token: Token | null
}

export interface Authentication {
  codeVerify: string
  state: string
  nonce: string
}

export interface Token {
  accessToken: string
  expiresAt: number
  idToken: string
  scope: string
  tokenType: string
}
