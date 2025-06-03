import useAuthStore, { type Token } from '@/store/AuthStore.ts'
import { enc, SHA256 } from 'crypto-js'

const authStore = useAuthStore()

const BASE_URL = import.meta.env.VITE_BASE_URL ?? ''
const CLIENT_ID = import.meta.env.VITE_CLIENT_ID ?? ''
const REDIRECT_URI = import.meta.env.VITE_REDIRECT_URI ?? ''
const SCOPE = import.meta.env.VITE_SCOPE ?? ''

interface Userinfo {
  sub: string
}

export async function signIn(callbackUrl?: string) {
  authStore.initAuthentication(callbackUrl ? enc.Base64.stringify(enc.Utf8.parse(callbackUrl as string)) : undefined)

  const codeVerify = authStore.authentication!.codeVerify
  const state = authStore.authentication!.state!
  const nonce = authStore.authentication!.nonce!
  const codeChallenge = enc.Base64url.stringify(SHA256(codeVerify))

  window.location.href = `${BASE_URL}/oauth2/authorize?response_type=code&client_id=${CLIENT_ID}&redirect_uri=${REDIRECT_URI}&scope=${SCOPE}&code_challenge_method=S256&code_challenge=${codeChallenge}&state=${state}&nonce=${nonce}`
}

export async function obtainToken(code: string, codeVerify: string) {
  const CLIENT_ID = import.meta.env.VITE_CLIENT_ID ?? ''
  const REDIRECT_URI = import.meta.env.VITE_REDIRECT_URI ?? ''

  const params = {
    grant_type: 'authorization_code',
    code: code,
    client_id: CLIENT_ID,
    redirect_uri: REDIRECT_URI,
    code_verifier: codeVerify
  }

  return fetch(`${BASE_URL}/oauth2/token`, { method: 'POST', body: new URLSearchParams(Object.entries(params)) })
    .then(res => res.json())
    .then(res => {
      const expireAt = new Date()
      expireAt.setSeconds(expireAt.getSeconds() + res.expires_in)
      return {
        accessToken: res.access_token,
        expiresAt: expireAt.getTime(),
        idToken: res.id_token,
        scope: res.scope,
        tokenType: res.token_type
      } as Token
    })
}

export async function findCurrentUserinfo() {
  return fetch(`${BASE_URL}/userinfo`, {
    method: 'GET',
    headers: { Authorization: `${authStore.token?.tokenType} ${authStore.token?.accessToken}` }
  }).then<Userinfo>(res => res.json())
}

export async function signOut() {
  authStore.destroyToken()
  const url = `${BASE_URL}/logout`
  window.location.replace(url)
}
