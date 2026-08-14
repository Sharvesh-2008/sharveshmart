import api from './api'

export function listOrders() {
  return api.get('/api/orders').then((response) => response.data)
}

export function getOrder(orderId) {
  return api.get(`/api/orders/${orderId}`).then((response) => response.data)
}

export function payOrder(orderId) {
  return api.post(`/api/orders/${orderId}/pay`).then((response) => response.data)
}
