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

const form = ref({ username: '', password: '', confirmPassword: '' })
const loading = ref(false)

async function onSubmit() {
  if (!form.value.username.trim()) {
    toast.error('请输入用户名')
    return
  }
  if (form.value.password.length < 6) {
    toast.error('密码长度不能少于 6 位')
    return
  }
  if (form.value.password !== form.value.confirmPassword) {
    toast.error('两次密码不一致')
    return
  }
  loading.value = true
  try {
    const data = await authStore.register(form.value)
    themeStore.initFromUser(data.user?.theme || 'system')
    toast.success('注册成功')
    await router.replace(resolveAuthRedirect(route.query.redirect))
  } catch (e) {
    toast.error(e.message || '注册失败')
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
        <h1 class="text-xl font-bold text-morandi-charcoal dark:text-morandi-dark-text">创建账号</h1>
        <p class="mt-1 text-sm text-neutral-500">加入 OCU.copilot</p>
      </div>

      <form class="space-y-4" @submit.prevent="onSubmit">
        <div>
          <label for="reg-username" class="mb-1 block text-sm font-medium text-neutral-700 dark:text-neutral-300">
            用户名
          </label>
          <input id="reg-username" v-model="form.username" type="text" class="input-field" placeholder="请输入用户名" />
        </div>
        <div>
          <label for="reg-password" class="mb-1 block text-sm font-medium text-neutral-700 dark:text-neutral-300">
            密码
          </label>
          <input
            id="reg-password"
            v-model="form.password"
            type="password"
            class="input-field"
            placeholder="至少 6 位"
          />
        </div>
        <div>
          <label for="reg-confirm" class="mb-1 block text-sm font-medium text-neutral-700 dark:text-neutral-300">
            确认密码
          </label>
          <input
            id="reg-confirm"
            v-model="form.confirmPassword"
            type="password"
            class="input-field"
            placeholder="再次输入密码"
          />
        </div>
        <button type="submit" class="btn-primary w-full" :disabled="loading">
          {{ loading ? '注册中…' : '注册' }}
        </button>
      </form>

      <p class="mt-6 text-center text-sm text-neutral-500">
        已有账号？
        <RouterLink
          to="/login"
          class="cursor-pointer font-medium text-morandi-clay transition-colors duration-200 hover:text-morandi-clay-hover"
        >
          去登录
        </RouterLink>
      </p>
    </div>
  </div>
</template>
