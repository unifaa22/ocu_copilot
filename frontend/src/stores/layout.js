import { defineStore } from 'pinia'
import { ref } from 'vue'

const SIDEBAR_KEY = 'kw_sidebar_collapsed'

export const useLayoutStore = defineStore('layout', () => {
  const sidebarCollapsed = ref(localStorage.getItem(SIDEBAR_KEY) === 'true')

  function persistSidebar() {
    localStorage.setItem(SIDEBAR_KEY, String(sidebarCollapsed.value))
  }

  function toggleSidebar() {
    sidebarCollapsed.value = !sidebarCollapsed.value
    persistSidebar()
  }

  function setSidebarCollapsed(collapsed) {
    sidebarCollapsed.value = collapsed
    persistSidebar()
  }

  return { sidebarCollapsed, toggleSidebar, setSidebarCollapsed }
})
