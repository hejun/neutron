import { ref } from 'vue'
import { defineStore } from 'pinia'

type Client = {
  clientId: string
  clientName: string
}

const useSystemStore = defineStore(
  'system',
  () => {
    const client = ref<Client | null>(null)
    const updateClient = (data: Client) => (client.value = data)
    const destroyClient = () => {
      client.value = null
    }

    const theme = ref('light')
    const toggleTheme = () => {
      theme.value = theme.value === 'light' ? 'dark' : 'light'
    }

    return {
      client,
      updateClient,
      destroyClient,
      theme,
      toggleTheme
    }
  },
  {
    persist: {
      storage: localStorage
    }
  }
)

export default useSystemStore
