import api from './api'

export function getSellerProducts(sellerId) {
  return api.get(`/api/sellers/${sellerId}/products`).then((response) => response.data)
}

export function createProduct(payload) {
  return api.post('/api/products', payload).then((response) => response.data)
}

export function updateProduct(productId, payload) {
  return api.put(`/api/products/${productId}`, payload).then((response) => response.data)
}

export function archiveProduct(productId) {
  return api.delete(`/api/products/${productId}`).then((response) => response.data)
}

export function submitProduct(productId) {
  return api.patch(`/api/products/${productId}/submit`).then((response) => response.data)
}
