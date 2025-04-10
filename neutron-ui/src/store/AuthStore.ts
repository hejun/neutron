import { defineStore } from 'pinia'
import type { Authentication, AuthStore, Token } from '#/AuthStore'

export default defineStore('auth', {
  state: (): AuthStore => ({
    authentication: null,
    token: null
  }),
  getters: {
    isAuthorized: (state: AuthStore): boolean => state.token !== null && state.token.expiresAt > new Date().getTime()
  },
  actions: {
    initAuthentication(oauthCallback?: string) {
      this.authentication = {
        codeVerify: Math.random().toString(36).slice(6),
        state: oauthCallback || '',
        nonce: Math.random().toString(36).slice(6)
      } as Authentication
    },
    destroyCodeVerify() {
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
