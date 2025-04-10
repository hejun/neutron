<script setup lang="ts">
import { ref } from 'vue'
import { useAuthStore } from '@/store'

const REQUEST_PREFIX = import.meta.env.VITE_REQUEST_PREFIX || ''
const authStore = useAuthStore()

const loginUserRef = ref<string>()

if (authStore.isAuthorized) {
  fetch(`${REQUEST_PREFIX}/userinfo`, {
    method: 'POST',
    headers: { Authorization: `${authStore.token?.tokenType} ${authStore.token?.accessToken}` }
  })
    .then(res => res.json())
    .then(res => loginUserRef.value = res.sub)
}
</script>

<template>
  <div>{{ loginUserRef }}</div>
</template>
