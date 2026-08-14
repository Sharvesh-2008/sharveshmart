import api from './api'

export function getCategories() {
  return api.get('/api/categories').then((response) => response.data)
}
