import { defineStore } from 'pinia'

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

export interface AuthState {
  authentication: Authentication | null
  token: Token | null
}

const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    authentication: null,
    token: null
  }),
  getters: {
    isAuthorized: (state: AuthState): boolean => state.token !== null && state.token.expiresAt > new Date().getTime()
  },
  actions: {
    initAuthentication(state?: string) {
      this.authentication = {
        codeVerify: Math.random().toString(36).slice(6),
        state: state ?? '',
        nonce: Math.random().toString(36).slice(6)
      } as Authentication
    },
    destroyAuthentication() {
      this.authentication = null
    },
    updateToken(token: Token) {
      this.token = token
    },
    destroyToken() {
      this.token = null
    }
  },
  persist: {
    storage: sessionStorage
  }
})

export default useAuthStore
