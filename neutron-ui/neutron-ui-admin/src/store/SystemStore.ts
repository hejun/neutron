import { ref } from 'vue'
import { defineStore } from 'pinia'

const useSystemStore = defineStore(
  'system',
  () => {
    const theme = ref('light')
    const toggleTheme = () => {
      theme.value = theme.value === 'light' ? 'dark' : 'light'
    }

    return {
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
