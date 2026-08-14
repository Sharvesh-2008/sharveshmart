import api from './api'

export function login(email, password) {
  return api.post('/api/auth/login', { email, password }).then((response) => response.data)
}

export function register(payload) {
  return api.post('/api/auth/register', payload).then((response) => response.data)
}