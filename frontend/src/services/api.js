import axios from 'axios'
import * as storage from '../utils/storage'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL ?? '',
})

api.interceptors.request.use((config) => {
  const token = storage.getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

api.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error.response?.status
    const url = error.config?.url ?? ''

    if (status === 401 && !url.includes('/api/auth/login')) {
      storage.clearSession()
      if (window.location.pathname !== '/login' && window.location.pathname !== '/unauthorized') {
        window.location.href = '/unauthorized'
      }
    } else if (status === 403 && !url.includes('/api/auth/')) {
      if (window.location.pathname !== '/forbidden') {
        window.location.href = '/forbidden'
      }
    }
    return Promise.reject(error)
  },
)

export default api