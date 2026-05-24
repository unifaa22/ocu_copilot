<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useThemeStore } from '@/stores/theme'
import { roleLabel } from '@/utils/format'

const router = useRouter()
const authStore = useAuthStore()
const themeStore = useThemeStore()
const showThemeDropdown = ref(false)

function avatarUrl() {
  return (
    authStore.user?.avatarUrl ||
    `https://api.dicebear.com/7.x/avataaars/svg?seed=${authStore.user?.username || 'user'}`
  )
}

function goProfile() {
  router.push({ name: 'profile' })
}

function setTheme(t) {
  themeStore.setTheme(t)
  showThemeDropdown.value = false
}
</script>

<template>
  <header
    class="sticky top-0 z-40 border-b border-morandi-peach/20 bg-white shadow-sm transition-colors duration-300 dark:bg-morandi-dark-card"
  >
    <div class="flex items-center justify-between px-6 py-3.5">
      <div class="flex items-center space-x-3">
        <div class="flex h-10 w-10 items-center justify-center rounded-xl bg-morandi-clay text-white shadow-md">
          <i class="fa-solid fa-graduation-cap text-xl" aria-hidden="true" />
        </div>
        <div>
          <h1 class="text-lg font-bold tracking-wide text-morandi-charcoal dark:text-morandi-dark-text">
            OCU.copilot
          </h1>
          <p class="text-xs font-medium text-morandi-clay dark:text-morandi-peach">
            高效学习 · 智能检索 · 小组协作
          </p>
        </div>
      </div>

      <div class="flex items-center space-x-6">
        <div class="relative">
          <button
            type="button"
            aria-label="切换主题"
            class="cursor-pointer rounded-lg border border-morandi-peach/15 bg-morandi-sand-dark p-2 transition-colors duration-200 hover:border-morandi-clay/50 dark:bg-morandi-dark-bg"
            @click="showThemeDropdown = !showThemeDropdown"
          >
            <i v-if="themeStore.theme === 'light'" class="fa-solid fa-sun text-amber-500" aria-hidden="true" />
            <i v-else-if="themeStore.theme === 'dark'" class="fa-solid fa-moon text-indigo-400" aria-hidden="true" />
            <i v-else class="fa-solid fa-desktop text-neutral-400" aria-hidden="true" />
          </button>
          <div
            v-if="showThemeDropdown"
            class="absolute right-0 z-50 mt-2 w-36 rounded-lg border border-morandi-peach/20 bg-white py-1 shadow-xl dark:border-morandi-peach/10 dark:bg-morandi-dark-card"
          >
            <button
              type="button"
              class="flex w-full cursor-pointer items-center space-x-2 px-4 py-2 text-sm transition-colors duration-200 hover:bg-morandi-sand dark:hover:bg-morandi-dark-bg"
              @click="setTheme('light')"
            >
              <i class="fa-solid fa-sun w-4 text-amber-500" aria-hidden="true" /><span>白天模式</span>
            </button>
            <button
              type="button"
              class="flex w-full cursor-pointer items-center space-x-2 px-4 py-2 text-sm transition-colors duration-200 hover:bg-morandi-sand dark:hover:bg-morandi-dark-bg"
              @click="setTheme('dark')"
            >
              <i class="fa-solid fa-moon w-4 text-indigo-400" aria-hidden="true" /><span>夜间模式</span>
            </button>
            <button
              type="button"
              class="flex w-full cursor-pointer items-center space-x-2 px-4 py-2 text-sm transition-colors duration-200 hover:bg-morandi-sand dark:hover:bg-morandi-dark-bg"
              @click="setTheme('system')"
            >
              <i class="fa-solid fa-desktop w-4 text-neutral-400" aria-hidden="true" /><span>跟随系统</span>
            </button>
          </div>
        </div>

        <button
          type="button"
          class="flex cursor-pointer items-center space-x-3 border-l border-morandi-peach/20 pl-6 transition-opacity duration-200 hover:opacity-80"
          @click="goProfile"
        >
          <img :src="avatarUrl()" class="h-9 w-9 rounded-full border-2 border-morandi-clay object-cover shadow-sm" alt="用户头像" />
          <div class="hidden text-left md:block">
            <div class="text-sm font-semibold tracking-wide text-morandi-charcoal dark:text-morandi-dark-text">
              {{ authStore.user?.username }}
            </div>
            <span class="rounded bg-morandi-peach/10 px-1.5 py-0.5 text-[10px] font-bold text-morandi-clay">
              {{ roleLabel(authStore.user?.roles) }}
            </span>
          </div>
        </button>
      </div>
    </div>
  </header>
</template>
