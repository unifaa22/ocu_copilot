import '@/assets/styles/main.css'
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import { initThemeBeforeMount, useThemeStore } from './stores/theme'
import { useAuthStore } from './stores/auth'
import { unregisterLegacyMockWorker } from '@/utils/unregisterMockWorker'

initThemeBeforeMount()

async function bootstrap() {
  const reloaded = await unregisterLegacyMockWorker()
  if (reloaded) return

  const app = createApp(App)
  const pinia = createPinia()
  app.use(pinia)
  app.use(router)

  const authStore = useAuthStore(pinia)
  const themeStore = useThemeStore(pinia)
  themeStore.initFromUser(authStore.user?.theme || localStorage.getItem('kw_theme') || 'system')

  app.mount('#app')
}

bootstrap()
