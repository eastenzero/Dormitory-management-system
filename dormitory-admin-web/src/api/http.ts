import axios, { AxiosError } from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'
import { clearToken, getToken } from '../utils/token'

const http = axios.create({
  timeout: 15000,
})

http.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers = config.headers || {}
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (res) => res,
  (err: AxiosError) => {
    const status = err.response?.status

    if (status === 401) {
      clearToken()
      if (router.currentRoute.value.path !== '/login') {
        router.replace('/login')
      }
      ElMessage.error('登录已失效，请重新登录')
    }

    if (status === 403) {
      ElMessage.warning('无权限访问')
    }

    return Promise.reject(err)
  },
)

export default http
