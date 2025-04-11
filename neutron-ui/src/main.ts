import { createApp } from 'vue'
import store from '@/store'
import router from '@/router'
import vuetify from '@/plugins/vuetify'
import App from '@/App.vue'

createApp(App).use(store).use(router).use(vuetify).mount('#app')
