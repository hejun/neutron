<script setup lang="ts">
import { ref } from 'vue'
import useAuthStore from '@/store/AuthStore'
import { RouterView } from 'vue-router'

const authStore = useAuthStore()

const BASE_URL = import.meta.env.VITE_BASE_URL ?? ''

const oAuthSignOut = () => {
  authStore.destroyToken()
  const url = `${BASE_URL}/logout`
  window.location.replace(url)
}

const userinfoRef = ref()
const loadUserinfo = () => {
  fetch(`${BASE_URL}/userinfo`, {
    method: 'GET',
    headers: { Authorization: `${authStore.token?.tokenType} ${authStore.token?.accessToken}` }
  })
    .then(res => res.json())
    .then(res => (userinfoRef.value = res))
}
</script>

<template>
  <div>
    <button type="button" @click="loadUserinfo">Load Userinfo</button>
    <button type="button" @click="oAuthSignOut">OAuth SignOut</button>
  </div>
  <div>{{ userinfoRef }}</div>
  <RouterView />
</template>
