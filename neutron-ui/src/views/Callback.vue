<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router'
import useAuthStore from '@/store/AuthStore'
import { enc } from 'crypto-js'
import { obtainToken, signIn } from '@/api/AuthorizationApi'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const { error, error_description, code, state, callbackUrl } = route.query

const oAuthSignIn = () => {
  signIn(callbackUrl ? (callbackUrl as string) : undefined)
}

const oAuthObtainToken = () => {
  const codeVerify = authStore.authentication ? authStore.authentication!.codeVerify : authStore.authentication

  if (!code || !codeVerify) {
    // eslint-disable-next-line no-alert
    alert(`Missing code or verify code`)
    return
  }

  authStore.destroyAuthentication()

  obtainToken(code as string, codeVerify)
    .then(res => authStore.updateToken(res))
    .then(() => {
      const url = state ? decodeURIComponent(enc.Utf8.stringify(enc.Base64.parse(state as string))) : '/'
      router.replace(url)
    })
}
</script>

<template>
  <div>
    <button type="button" @click="oAuthSignIn">OAuth SignIn</button>
    <button type="button" @click="oAuthObtainToken">Obtain Token</button>
  </div>
  <div>
    <p>{{ error }}</p>
    <p>{{ error_description }}</p>
  </div>
</template>
