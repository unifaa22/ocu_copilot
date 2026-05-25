<script setup>
import { ref } from 'vue'
import { useRouter, useRoute, RouterLink } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { resolveAuthRedirect } from '@/utils/authRedirect'
import { useThemeStore } from '@/stores/theme'
import { useToast } from '@/composables/useToast'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const themeStore = useThemeStore()
const toast = useToast()

const form = ref({ username: '', password: '' })
const loading = ref(false)

async function onSubmit() {
  if (!form.value.username.trim()) {
    toast.error('请输入用户名')
    return
  }
  if (!form.value.password) {
    toast.error('请输入密码')
    return
  }
  loading.value = true
  try {
    const data = await authStore.login(form.value)
    themeStore.initFromUser(data.user?.theme || 'system')
    toast.success('登录成功')
    await router.replace(resolveAuthRedirect(route.query.redirect))
  } catch (e) {
    toast.error(e.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="flex min-h-screen items-center justify-center bg-morandi-sand p-6 dark:bg-morandi-dark-bg">
    <div
      class="w-full max-w-md rounded-2xl border border-morandi-peach/25 bg-white p-8 shadow-lg transition-colors duration-200 dark:border-morandi-peach/10 dark:bg-morandi-dark-card"
    >
      <div class="mb-8 text-center">
        <div
          class="mx-auto mb-4 flex h-14 w-14 items-center justify-center rounded-2xl bg-morandi-clay text-white shadow-md"
        >
          <i class="fa-solid fa-graduation-cap text-2xl" aria-hidden="true" />
        </div>
        <h1 class="text-xl font-bold text-morandi-charcoal dark:text-morandi-dark-text">OCU.copilot</h1>
        <p class="mt-1 text-sm text-neutral-500">登录以继续学习</p>
      </div>

      <form class="space-y-4" @submit.prevent="onSubmit">
        <div>
          <label for="username" class="mb-1 block text-sm font-medium text-neutral-700 dark:text-neutral-300">
            用户名
          </label>
          <input
            id="username"
            v-model="form.username"
            type="text"
            autocomplete="username"
            class="input-field"
            placeholder="请输入用户名"
          />
        </div>
        <div>
          <label for="password" class="mb-1 block text-sm font-medium text-neutral-700 dark:text-neutral-300">
            密码
          </label>
          <input
            id="password"
            v-model="form.password"
            type="password"
            autocomplete="current-password"
            class="input-field"
            placeholder="至少 6 位"
          />
        </div>
        <button type="submit" class="btn-primary w-full" :disabled="loading">
          <i v-if="loading" class="fa-solid fa-spinner fa-spin mr-2" aria-hidden="true" />
          {{ loading ? '登录中…' : '登录' }}
        </button>
      </form>

      <p class="mt-6 text-center text-sm text-neutral-500">
        还没有账号？
        <RouterLink
          to="/register"
          class="cursor-pointer font-medium text-morandi-clay transition-colors duration-200 hover:text-morandi-clay-hover"
        >
          立即注册
        </RouterLink>
      </p>
    </div>
  </div>
</template>
