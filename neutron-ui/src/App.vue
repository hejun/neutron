<script setup lang="ts">
import { RouterView } from 'vue-router'
import { useSettingsStore } from '@/store'

const settingsStore = useSettingsStore()
</script>

<template>
  <VApp :theme="settingsStore.theme">
    <RouterView />
  </VApp>
</template>
