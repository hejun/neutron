import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'

const routes: Readonly<RouteRecordRaw[]> = [
  {
    name: 'Home',
    path: '/',
    redirect: '/dashboard'
  },
  {
    name: 'Dashboard',
    path: '/dashboard',
    component: () => import('@/views/dashboard/Dashboard.vue')
  },
  {
    name: 'Setting',
    path: '/setting',
    redirect: '/setting/tenant',
    children: [
      {
        name: 'Tenant',
        path: 'tenant',
        component: () => import('@/views/setting/Tenant.vue')
      },
      {
        name: 'Client',
        path: 'client',
        component: () => import('@/views/setting/Client.vue')
      },
      {
        name: 'User',
        path: 'user',
        component: () => import('@/views/setting/User.vue')
      }
    ]
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

export default router
