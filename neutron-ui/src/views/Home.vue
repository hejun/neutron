<script setup lang="ts">
import { ref } from 'vue'
import { useRoute } from 'vue-router'
import { signIn, signOut, findUserinfo } from '@/api/AuthorizationApi.ts'

const route = useRoute()
const respRef = ref()

function execSignIn() {
  signIn(route.fullPath)
}

function execFindUserinfo() {
  respRef.value = null
  findUserinfo().then(userinfo => (respRef.value = userinfo))
}
</script>

<template>
  <div>
    <button type="button" @click="execSignIn">登录</button>
    <button type="button" @click="signOut">登出</button>
  </div>
  <div>
    <button type="button" @click="execFindUserinfo">登录用户信息</button>
  </div>
  <pre>{{ respRef }}</pre>
</template>
