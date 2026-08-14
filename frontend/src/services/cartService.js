import api from './api'

export function getCart() {
  return api.get('/api/cart').then((response) => response.data)
}

export function addCartItem(productId, quantity) {
  return api.post('/api/cart/items', { productId, quantity }).then((response) => response.data)
}

export function updateCartItem(productId, quantity) {
  return api.put(`/api/cart/items/${productId}`, { quantity }).then((response) => response.data)
}

export function removeCartItem(productId) {
  return api.delete(`/api/cart/items/${productId}`).then((response) => response.data)
}

export function checkout() {
  return api.post('/api/orders/checkout').then((response) => response.data)
}

export function payOrder(orderId) {
  return api.post(`/api/orders/${orderId}/pay`).then((response) => response.data)
}

export function getOrder(orderId) {
  return api.get(`/api/orders/${orderId}`).then((response) => response.data)
}
