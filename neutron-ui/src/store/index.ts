import { createPinia } from 'pinia'
import piniaPluginPersistedState from 'pinia-plugin-persistedstate'
import useAuthStore from '@/store/AuthStore'
import useSettingsStore from '@/store/SettingsStore'

const pinia = createPinia()
pinia.use(piniaPluginPersistedState)

export { useAuthStore, useSettingsStore }

export default pinia
