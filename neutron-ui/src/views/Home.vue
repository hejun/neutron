<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useSettingsStore } from '@/store'
import { useAuthStore } from '@/store'

const router = useRouter()
const settingsStore = useSettingsStore()
const authStore = useAuthStore()

const toggleCollapse = () => settingsStore.toggleCollapse()

const toggleTheme = () => settingsStore.toggleTheme()

const destroy = () => {
  authStore.destroyToken()
  router.replace('/callback')
}
</script>

<template>
  <VLayout>
    <VAppBar elevation="0" border>
      <template v-slot:prepend>
        <VAppBarNavIcon @click.native="toggleCollapse" />
      </template>
      <VAppBarTitle>Neutron</VAppBarTitle>
      <template v-slot:append>
        <VBtn
          :icon="settingsStore.theme === 'light' ? 'mdi-weather-sunny' : 'mdi-weather-night'"
          @click="toggleTheme"
        />
        <VDivider vertical class="mx-4" />
        <VBtn prepend-icon="mdi-logout" type="button" @click="destroy">Logout</VBtn>
      </template>
    </VAppBar>
    <VMain>
      <VNavigationDrawer :model-value="settingsStore.collapsed">
        <VList nav></VList>
      </VNavigationDrawer>
    </VMain>
  </VLayout>
</template>
