import { createPinia } from 'pinia'
import piniaPluginPersistedState from 'pinia-plugin-persistedstate'
import useAuthStore from '@/store/AuthStore'

const pinia = createPinia()
pinia.use(piniaPluginPersistedState)

export { useAuthStore }

export default pinia
