import api from './api'

export function listPendingProducts() {
  return api.get('/api/admin/products/pending').then((response) => response.data)
}

export function approveProduct(productId) {
  return api.post(`/api/admin/products/${productId}/approve`).then((response) => response.data)
}

export function rejectProduct(productId) {
  return api.post(`/api/admin/products/${productId}/reject`).then((response) => response.data)
}
