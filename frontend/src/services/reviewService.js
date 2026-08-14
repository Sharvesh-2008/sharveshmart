import api from './api'

export function getProductReviews(productId) {
  return api.get(`/api/products/${productId}/reviews`).then((response) => response.data)
}

export function createReview(productId, payload) {
  return api.post(`/api/products/${productId}/reviews`, payload).then((response) => response.data)
}

export function updateReview(reviewId, payload) {
  return api.put(`/api/reviews/${reviewId}`, payload).then((response) => response.data)
}

export function deleteReview(reviewId) {
  return api.delete(`/api/reviews/${reviewId}`).then((response) => response.data)
}
