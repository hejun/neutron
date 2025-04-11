import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueJsx from '@vitejs/plugin-vue-jsx'
import path from 'path'
import vuetify from 'vite-plugin-vuetify'

export default defineConfig({
  plugins: [vue(), vueJsx(), vuetify({ autoImport: true, styles: { configFile: 'src/styles/settings.scss' } })],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src'),
      '#': path.resolve(__dirname, 'types')
    }
  },
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:9000',
        changeOrigin: true,
        rewrite: path => path.replace(/^\/api/, '')
      }
    }
  }
})
