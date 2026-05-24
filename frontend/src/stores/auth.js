import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi, userApi } from '@/api'

const TOKEN_KEY = 'kw_access_token'
const USER_KEY = 'kw_user'
export const DEV_BYPASS_TOKEN = 'dev-bypass-token'

export const skipAuth = import.meta.env.VITE_SKIP_AUTH === 'true'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem(TOKEN_KEY) || '')
  const user = ref(JSON.parse(localStorage.getItem(USER_KEY) || 'null'))

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

  /** 开发期免登录：写入本地会话，不调用登录/注册 Mock 接口 */
  function initDevBypass() {
    if (!skipAuth || isLoggedIn.value) return
    setSession(DEV_BYPASS_TOKEN, {
      id: 1,
      username: 'dev',
      avatar: null,
      avatarUrl: 'https://api.dicebear.com/7.x/avataaars/svg?seed=dev',
      theme: localStorage.getItem('kw_theme') || 'system',
      roles: ['USER', 'TEAM_CREATOR'],
    })
  }

  async function login(credentials) {
    const data = await authApi.login(credentials)
    setSession(data.accessToken, data.user)
    return data
  }

  async function register(credentials) {
    const data = await authApi.register(credentials)
    setSession(data.accessToken, data.user)
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
    if (skipAuth) initDevBypass()
  }

  return {
    token,
    user,
    isLoggedIn,
    isTeamCreator,
    initDevBypass,
    login,
    register,
    fetchProfile,
    updateUser,
    logout,
    setSession,
  }
})
