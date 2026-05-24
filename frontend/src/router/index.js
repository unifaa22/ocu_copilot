import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore, skipAuth } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/auth/LoginView.vue'),
      meta: { requiresAuth: false },
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('@/views/auth/RegisterView.vue'),
      meta: { requiresAuth: false },
    },
    {
      path: '/',
      component: () => import('@/components/layout/MainLayout.vue'),
      meta: { requiresAuth: true },
      children: [
        { path: '', redirect: { name: 'home' } },
        {
          path: 'home',
          name: 'home',
          component: () => import('@/views/home/HomeView.vue'),
        },
        {
          path: 'knowledge',
          name: 'knowledge',
          component: () => import('@/views/knowledge/KnowledgeView.vue'),
        },
        {
          path: 'chat/personal',
          name: 'chat-personal',
          component: () => import('@/views/chat/PersonalChatView.vue'),
        },
        {
          path: 'chat/team',
          name: 'chat-team',
          component: () => import('@/views/chat/TeamChatView.vue'),
        },
        {
          path: 'notes',
          name: 'notes',
          component: () => import('@/views/notes/NotesView.vue'),
        },
        {
          path: 'team',
          name: 'team',
          component: () => import('@/views/team/TeamView.vue'),
        },
        {
          path: 'profile',
          name: 'profile',
          component: () => import('@/views/profile/ProfileView.vue'),
        },
      ],
    },
  ],
})

router.beforeEach((to) => {
  const authStore = useAuthStore()

  if (skipAuth) {
    authStore.initDevBypass()
    if (to.name === 'login' || to.name === 'register') {
      return { path: '/home' }
    }
    return true
  }

  const requiresAuth = to.matched.some((r) => r.meta.requiresAuth !== false)

  if (requiresAuth && !authStore.isLoggedIn) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }
  if ((to.name === 'login' || to.name === 'register') && authStore.isLoggedIn) {
    return { path: '/home' }
  }
  return true
})

export default router
