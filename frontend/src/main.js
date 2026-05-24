import '@/assets/styles/main.css'
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import { initThemeBeforeMount, useThemeStore } from './stores/theme'
import { useAuthStore } from './stores/auth'

initThemeBeforeMount()

async function bootstrap() {
  if (import.meta.env.VITE_USE_MOCK === 'true') {
    const { startMockServer } = await import('./mocks/browser')
    await startMockServer()
  }

  const app = createApp(App)
  const pinia = createPinia()
  app.use(pinia)
  app.use(router)

  const authStore = useAuthStore(pinia)
  authStore.initDevBypass()
  const themeStore = useThemeStore(pinia)
  themeStore.initFromUser(authStore.user?.theme || localStorage.getItem('kw_theme') || 'system')

  app.mount('#app')
}

bootstrap()
