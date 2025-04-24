<script setup lang="ts">
import { ref } from 'vue'
import { RouterView } from 'vue-router'
import { findCurrentUserinfo, signOut } from '@/api/AuthorizationApi.ts'

const userinfoRef = ref()

const loadUserinfo = () => {
  findCurrentUserinfo().then(res => (userinfoRef.value = res))
}
</script>

<template>
  <div>
    <button type="button" @click="loadUserinfo">Load Userinfo</button>
    <button type="button" @click="signOut">OAuth SignOut</button>
  </div>
  <div>{{ userinfoRef }}</div>
  <RouterView />
</template>
