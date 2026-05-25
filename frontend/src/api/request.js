import axios from 'axios'
import { useAuthStore } from '@/stores/auth'
import router from '@/router'

const TOKEN_KEY = 'kw_access_token'

const request = axios.create({
  baseURL: '/api',
  timeout: 60000,
})

function getAccessToken() {
  const authStore = useAuthStore()
  return authStore.token || localStorage.getItem(TOKEN_KEY) || ''
}

request.interceptors.request.use((config) => {
  const token = getAccessToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

request.interceptors.response.use(
  (response) => {
    const result = response.data
    if (result.code === 401) {
      const authStore = useAuthStore()
      const onLoginPage = router.currentRoute.value.name === 'login'
      authStore.logout()
      if (!onLoginPage) {
        router.replace({
          name: 'login',
          query: { redirect: router.currentRoute.value.fullPath },
        })
      }
      return Promise.reject(new Error(result.message || '未认证，请先登录'))
    }
    if (result.code !== 200) {
      return Promise.reject(new Error(result.message || '请求失败'))
    }
    return result.data
  },
  (error) => {
    const status = error.response?.status
    const body = error.response?.data
    const message = body?.message || error.message || '网络错误'
    if (status === 401 || body?.code === 401) {
      const authStore = useAuthStore()
      authStore.logout()
      if (router.currentRoute.value.name !== 'login') {
        router.replace({
          name: 'login',
          query: { redirect: router.currentRoute.value.fullPath },
        })
      }
    }
    return Promise.reject(new Error(message))
  },
)

export default request
