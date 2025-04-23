<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router'
import useAuthStore from '@/store/AuthStore'
import { enc, SHA256 } from 'crypto-js'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const BASE_URL = import.meta.env.VITE_BASE_URL ?? ''
const CLIENT_ID = import.meta.env.VITE_CLIENT_ID ?? ''
const REDIRECT_URI = import.meta.env.VITE_REDIRECT_URI ?? ''
const SCOPE = import.meta.env.VITE_SCOPE ?? ''

const { error, error_description, code, state, callbackUrl } = route.query

const oAuthSignIn = () => {
  authStore.initAuthentication(callbackUrl ? enc.Base64.stringify(enc.Utf8.parse(callbackUrl as string)) : undefined)

  const codeVerify = authStore.authentication!.codeVerify
  const state = authStore.authentication!.state!
  const nonce = authStore.authentication!.nonce!
  const codeChallenge = enc.Base64url.stringify(SHA256(codeVerify))

  window.location.href = `${BASE_URL}/oauth2/authorize?response_type=code&client_id=${CLIENT_ID}&redirect_uri=${REDIRECT_URI}&scope=${SCOPE}&code_challenge_method=S256&code_challenge=${codeChallenge}&state=${state}&nonce=${nonce}`
}
const obtainToken = () => {
  const codeVerify = authStore.authentication!.codeVerify!
  authStore.destroyAuthentication()

  const url = `${BASE_URL}/oauth2/token`
  const params = {
    grant_type: 'authorization_code',
    code: code as string,
    client_id: CLIENT_ID,
    redirect_uri: REDIRECT_URI,
    code_verifier: codeVerify as string
  }
  const body = new URLSearchParams(Object.entries(params))

  fetch(url, { method: 'POST', body })
    .then(res => res.json())
    .then(res => {
      const expireAt = new Date()
      expireAt.setSeconds(expireAt.getSeconds() + res.expires_in)
      const token = {
        accessToken: res.access_token,
        expiresAt: expireAt.getTime(),
        idToken: res.id_token,
        scope: res.scope,
        tokenType: res.token_type
      }
      authStore.updateToken(token)
    })
    .then(() => {
      const url = state ? decodeURIComponent(enc.Utf8.stringify(enc.Base64.parse(state as string))) : '/'
      router.replace(url)
    })
}
</script>

<template>
  <div>
    <button type="button" @click="oAuthSignIn">OAuth SignIn</button>
    <button type="button" @click="obtainToken">Obtain Token</button>
  </div>
  <div>
    <p>{{ error }}</p>
    <p>{{ error_description }}</p>
  </div>
</template>
