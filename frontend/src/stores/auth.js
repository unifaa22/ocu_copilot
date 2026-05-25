import { defineStore } from 'pinia'
import { ref, computed, nextTick } from 'vue'
import { authApi, userApi } from '@/api'

const TOKEN_KEY = 'kw_access_token'
const USER_KEY = 'kw_user'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem(TOKEN_KEY) || '')
  const user = ref(JSON.parse(localStorage.getItem(USER_KEY) || 'null'))

  // 清除历史开发 bypass 令牌，避免拦截真实登录流程
  if (token.value === 'dev-bypass-token' || token.value.startsWith('mock-token-')) {
    token.value = ''
    user.value = null
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_KEY)
  }

  const isLoggedIn = computed(() => !!token.value)
  const isTeamCreator = computed(() => user.value?.roles?.includes('TEAM_CREATOR'))

  function persist() {
    if (token.value) {
      localStorage.setItem(TOKEN_KEY, token.value)
    } else {
      localStorage.removeItem(TOKEN_KEY)
    }
    if (user.value) {
      localStorage.setItem(USER_KEY, JSON.stringify(user.value))
    } else {
      localStorage.removeItem(USER_KEY)
    }
  }

  function setSession(accessToken, userData) {
    token.value = accessToken
    user.value = userData
    persist()
  }

  async function login(credentials) {
    const data = await authApi.login(credentials)
    if (!data?.accessToken) {
      throw new Error('登录响应异常，未获取到令牌')
    }
    setSession(data.accessToken, data.user)
    await nextTick()
    return data
  }

  async function register(credentials) {
    const data = await authApi.register(credentials)
    if (!data?.accessToken) {
      throw new Error('注册响应异常，未获取到令牌')
    }
    setSession(data.accessToken, data.user)
    await nextTick()
    return data
  }

  async function fetchProfile() {
    const profile = await userApi.getProfile()
    user.value = profile
    persist()
    return profile
  }

  function updateUser(partial) {
    user.value = { ...user.value, ...partial }
    persist()
  }

  function logout() {
    token.value = ''
    user.value = null
    persist()
  }

  return {
    token,
    user,
    isLoggedIn,
    isTeamCreator,
    login,
    register,
    fetchProfile,
    updateUser,
    logout,
    setSession,
  }
})
