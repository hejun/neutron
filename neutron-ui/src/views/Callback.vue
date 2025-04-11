<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/store'
import { enc, SHA256 } from 'crypto-js'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const REQUEST_PREFIX = import.meta.env.VITE_REQUEST_PREFIX || ''
const CLIENT_ID = import.meta.env.VITE_CLIENT_ID || ''
const REDIRECT_URI = import.meta.env.VITE_REDIRECT_URI || ''
const SCOPE = import.meta.env.VITE_SCOPE || ''

const { error, error_description, code, state } = route.query

const hasError = ref(false)
const headlineIcon = ref('mdi-account-key-outline')
const title = ref('You need to sign in')
const text = ref()

if (error) {
  hasError.value = true
  headlineIcon.value = 'mdi-account-alert-outline'
  title.value = error as string
  text.value = error_description as string
} else if (code) {
  headlineIcon.value = 'mdi-account-check-outline'
  title.value = 'Sign in success'
}

const doOAuthLogin = () => {
  const { oauthCallback } = route.query
  authStore.initAuthentication(
    oauthCallback ? enc.Base64.stringify(enc.Utf8.parse(oauthCallback as string)) : undefined
  )
  const codeVerify = authStore.authentication!.codeVerify
  const state = authStore.authentication!.state!
  const nonce = authStore.authentication!.nonce!
  const codeChallenge = enc.Base64url.stringify(SHA256(codeVerify))

  window.location.href = `${REQUEST_PREFIX}/oauth2/authorize?response_type=code&client_id=${CLIENT_ID}&redirect_uri=${REDIRECT_URI}&scope=${SCOPE}&code_challenge_method=S256&code_challenge=${codeChallenge}&state=${state}&nonce=${nonce}`
}

const doObtainToken = () => {
  if (!code) {
    hasError.value = true
    headlineIcon.value = 'mdi-alert-circle-outline'
    title.value = 'Param: Code is not exists'
  }
  if (!authStore.authentication) {
    hasError.value = true
    headlineIcon.value = 'mdi-alert-circle-outline'
    title.value = 'Param: Authentication info is not exists'
  }
  const codeVerify = authStore.authentication!.codeVerify!
  authStore.destroyCodeVerify()
  if (!codeVerify) {
    hasError.value = true
    headlineIcon.value = 'mdi-alert-circle-outline'
    title.value = 'Param: CodeVerify info is not exists'
  }

  const url = `${REQUEST_PREFIX}/oauth2/token`
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

const backHome = () => {
  hasError.value = false
  headlineIcon.value = 'mdi-account-key-outline'
  title.value = 'You need to sign in'
  text.value = undefined
  router.replace('/')
}
</script>

<template>
  <VEmptyState :title="title" :text="text">
    <template #headline>
      <VIcon :icon="headlineIcon" :color="hasError ? 'red-darken-2' : undefined" />
    </template>
    <template #actions>
      <VBtn v-if="!code" color="blue" prepend-icon="mdi-login" @click="doOAuthLogin">Sign in</VBtn>
      <VBtn v-if="code" color="blue" prepend-icon="mdi-account-arrow-down" @click="doObtainToken">Obtain Token</VBtn>
      <VBtn v-if="hasError" color="white" prepend-icon="mdi-home-outline" @click="backHome">Back Home</VBtn>
    </template>
  </VEmptyState>
</template>
