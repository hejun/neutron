import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/store'

const routes: Readonly<RouteRecordRaw[]> = [
  {
    name: 'Home',
    path: '/',
    component: () => import('@/views/Home.vue'),
    meta: {
      requireAuthentication: true
    }
  },
  {
    name: 'Callback',
    path: '/callback',
    component: () => import('@/views/Callback.vue')
  },
  {
    name: '404',
    path: '/:pathMatch(.*)*',
    component: () => import('@/views/404.vue')
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

router.beforeEach((to, from, next) => {
  if (to.meta.requireAuthentication) {
    const authStore = useAuthStore()
    if (authStore.isAuthorized) {
      next()
    } else {
      next({
        path: '/callback',
        query: { oauthCallback: encodeURIComponent(to.fullPath) }
      })
    }
  } else {
    next()
  }
})

export default router
