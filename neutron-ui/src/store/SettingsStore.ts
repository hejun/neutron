import { defineStore } from 'pinia'
import type { SettingsStore } from '#/SettingsStore'

export default defineStore('settings', {
  state: (): SettingsStore => ({
    theme: 'light',
    collapsed: true
  }),
  actions: {
    toggleTheme() {
      this.theme = this.theme === 'light' ? 'dark' : 'light'
    },
    toggleCollapse() {
      this.collapsed = !this.collapsed
    }
  },
  persist: {
    storage: localStorage
  }
})
