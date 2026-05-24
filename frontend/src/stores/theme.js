import { defineStore } from 'pinia'
import { ref, watch } from 'vue'
import { usePreferredDark } from '@vueuse/core'
import { userApi } from '@/api'
import { useAuthStore } from './auth'

const THEME_KEY = 'kw_theme'

export const useThemeStore = defineStore('theme', () => {
  const theme = ref(localStorage.getItem(THEME_KEY) || 'system')
  const preferredDark = usePreferredDark()

  function resolveDark() {
    if (theme.value === 'dark') return true
    if (theme.value === 'light') return false
    return preferredDark.value
  }

  function applyTheme() {
    const isDark = resolveDark()
    const root = document.documentElement
    root.classList.toggle('dark', isDark)
    root.classList.toggle('light', !isDark)
    root.style.colorScheme = isDark ? 'dark' : 'light'
    localStorage.setItem(THEME_KEY, theme.value)
  }

  function initFromUser(userTheme) {
    if (userTheme) {
      theme.value = userTheme
      localStorage.setItem(THEME_KEY, userTheme)
    }
    applyTheme()
  }

  async function setTheme(newTheme) {
    theme.value = newTheme
    applyTheme()
    const authStore = useAuthStore()
    if (authStore.isLoggedIn) {
      try {
        await userApi.updateTheme(newTheme)
        authStore.updateUser({ theme: newTheme })
      } catch {
        /* 本地主题已生效 */
      }
    }
  }

  watch(preferredDark, () => {
    if (theme.value === 'system') applyTheme()
  })

  return { theme, initFromUser, setTheme, applyTheme, resolveDark }
})

/** 在 Vue 挂载前同步应用主题，避免闪烁 */
export function initThemeBeforeMount() {
  const stored = localStorage.getItem(THEME_KEY) || 'system'
  const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches
  const isDark = stored === 'dark' || (stored === 'system' && prefersDark)
  const root = document.documentElement
  root.classList.toggle('dark', isDark)
  root.classList.toggle('light', !isDark)
  root.style.colorScheme = isDark ? 'dark' : 'light'
}
