import api from './api'

export function getProducts() {
  return api.get('/api/products').then((response) => response.data)
}

export function getProductsByCategory(categoryId) {
  return api.get('/api/products', { params: { categoryId } }).then((response) => response.data)
}

export function getProduct(productId) {
  return api.get(`/api/products/${productId}`).then((response) => response.data)
}