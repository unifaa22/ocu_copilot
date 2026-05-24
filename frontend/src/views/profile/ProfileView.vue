<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { userApi } from '@/api'
import { useAuthStore } from '@/stores/auth'
import { useThemeStore } from '@/stores/theme'
import { useToast } from '@/composables/useToast'
import { roleLabel } from '@/utils/format'

const router = useRouter()
const authStore = useAuthStore()
const themeStore = useThemeStore()
const toast = useToast()

const passwordForm = ref({ oldPassword: '', newPassword: '', confirmPassword: '' })
const changingPassword = ref(false)
const uploadingAvatar = ref(false)
const avatarInput = ref(null)

function avatarUrl() {
  return (
    authStore.user?.avatarUrl ||
    `https://api.dicebear.com/7.x/avataaars/svg?seed=${authStore.user?.username || 'user'}`
  )
}

function triggerAvatar() {
  avatarInput.value?.click()
}

async function onAvatarChange(e) {
  const file = e.target.files?.[0]
  e.target.value = ''
  if (!file) return
  if (file.size > 2 * 1024 * 1024) {
    toast.error('头像不能超过 2MB')
    return
  }
  uploadingAvatar.value = true
  try {
    const data = await userApi.uploadAvatar(file)
    authStore.updateUser({ avatar: data.avatar, avatarUrl: data.avatarUrl })
    toast.success('头像已更新')
  } catch (err) {
    toast.error(err.message)
  } finally {
    uploadingAvatar.value = false
  }
}

async function changePassword() {
  if (passwordForm.value.newPassword.length < 6) {
    toast.error('新密码至少 6 位')
    return
  }
  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    toast.error('两次新密码不一致')
    return
  }
  changingPassword.value = true
  try {
    await userApi.updatePassword(passwordForm.value)
    toast.success('密码修改成功')
    passwordForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
  } catch (e) {
    toast.error(e.message)
  } finally {
    changingPassword.value = false
  }
}

function setTheme(t) {
  themeStore.setTheme(t)
}

function logout() {
  authStore.logout()
  router.push({ name: 'login' })
}

onMounted(() => {
  authStore.fetchProfile().catch(() => {})
})
</script>

<template>
  <div class="mx-auto max-w-2xl space-y-6">
    <section
      class="rounded-xl border border-morandi-peach/20 bg-white p-6 dark:border-morandi-peach/10 dark:bg-morandi-dark-card"
    >
      <div class="flex items-center gap-6">
        <div class="relative">
          <img :src="avatarUrl()" alt="头像" class="h-20 w-20 rounded-full border-2 border-morandi-clay object-cover" />
          <button
            type="button"
            aria-label="上传头像"
            class="absolute -right-1 -bottom-1 cursor-pointer rounded-full bg-morandi-clay p-2 text-white shadow transition-colors duration-200 hover:bg-morandi-clay-hover"
            :disabled="uploadingAvatar"
            @click="triggerAvatar"
          >
            <i class="fa-solid fa-camera text-xs" aria-hidden="true" />
          </button>
          <input ref="avatarInput" type="file" accept="image/jpeg,image/png" class="hidden" @change="onAvatarChange" />
        </div>
        <div>
          <p class="text-lg font-semibold">{{ authStore.user?.username }}</p>
          <span class="rounded bg-morandi-peach/15 px-2 py-0.5 text-xs text-morandi-clay">
            {{ roleLabel(authStore.user?.roles) }}
          </span>
        </div>
      </div>
    </section>

    <section
      class="rounded-xl border border-morandi-peach/20 bg-white p-6 dark:border-morandi-peach/10 dark:bg-morandi-dark-card"
    >
      <h3 class="mb-4 font-semibold text-morandi-charcoal dark:text-morandi-dark-text">界面主题</h3>
      <div class="flex flex-wrap gap-3">
        <button
          v-for="opt in [
            { key: 'light', label: '白天', icon: 'fa-sun' },
            { key: 'dark', label: '夜间', icon: 'fa-moon' },
            { key: 'system', label: '跟随系统', icon: 'fa-desktop' },
          ]"
          :key="opt.key"
          type="button"
          class="cursor-pointer rounded-lg border px-4 py-2 text-sm transition-colors duration-200"
          :class="
            themeStore.theme === opt.key
              ? 'border-morandi-clay bg-morandi-clay/10 text-morandi-clay'
              : 'border-morandi-peach/20 hover:bg-morandi-sand dark:hover:bg-morandi-dark-bg'
          "
          @click="setTheme(opt.key)"
        >
          <i class="fa-solid mr-2" :class="opt.icon" aria-hidden="true" />
          {{ opt.label }}
        </button>
      </div>
    </section>

    <section
      class="rounded-xl border border-morandi-peach/20 bg-white p-6 dark:border-morandi-peach/10 dark:bg-morandi-dark-card"
    >
      <h3 class="mb-4 font-semibold text-morandi-charcoal dark:text-morandi-dark-text">修改密码</h3>
      <form class="space-y-3" @submit.prevent="changePassword">
        <input v-model="passwordForm.oldPassword" type="password" class="input-field" placeholder="当前密码" />
        <input v-model="passwordForm.newPassword" type="password" class="input-field" placeholder="新密码（至少 6 位）" />
        <input v-model="passwordForm.confirmPassword" type="password" class="input-field" placeholder="确认新密码" />
        <button type="submit" class="btn-primary" :disabled="changingPassword">
          {{ changingPassword ? '提交中…' : '更新密码' }}
        </button>
      </form>
    </section>

    <button
      type="button"
      class="w-full cursor-pointer rounded-xl border border-red-200 bg-red-50 py-3 text-sm font-medium text-red-600 transition-colors duration-200 hover:bg-red-100 dark:border-red-900/30 dark:bg-red-950/20 dark:text-red-400"
      @click="logout"
    >
      <i class="fa-solid fa-right-from-bracket mr-2" aria-hidden="true" />
      退出登录
    </button>
  </div>
</template>
