import { useCallback, useEffect, useMemo, useState } from 'react'
import { useAuth } from '../../hooks/useAuth'
import { createReview, deleteReview, getProductReviews, updateReview } from '../../services/reviewService'
import StarRating from './StarRating'
import LoadingSpinner from '../ui/LoadingSpinner'
import ErrorState from '../ui/ErrorState'
import EmptyState from '../ui/EmptyState'

const inputClass =
  'mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 text-sm shadow-sm focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500'

function formatDate(value) {
  if (!value) return ''
  return new Date(value).toLocaleDateString(undefined, {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
  })
}

function ReviewForm({ rating, comment, onRatingChange, onCommentChange, onSubmit, onCancel, submitting, isEdit, error }) {
  return (
    <form onSubmit={onSubmit} className="rounded-lg border border-gray-200 bg-white p-4 shadow-sm">
      {error ? <ErrorState message={error} /> : null}
      <div>
        <span className="block text-sm font-medium text-gray-700">Rating</span>
        <div className="mt-2 flex items-center gap-1" role="radiogroup" aria-label="Rating">
          {[1, 2, 3, 4, 5].map((value) => (
            <button
              key={value}
              type="button"
              role="radio"
              aria-checked={rating === value}
              aria-label={`${value} star${value === 1 ? '' : 's'}`}
              onClick={() => onRatingChange(value)}
              className="focus:outline-none"
            >
              <svg
                className={`h-6 w-6 ${value <= rating ? 'text-amber-400' : 'text-gray-300'}`}
                fill="currentColor"
                viewBox="0 0 20 20"
                xmlns="http://www.w3.org/2000/svg"
                aria-hidden="true"
              >
                <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.286 3.958a1 1 0 00.95.69h4.162c.969 0 1.371 1.24.588 1.81l-3.367 2.446a1 1 0 00-.364 1.118l1.286 3.958c.3.922-.755 1.688-1.539 1.118l-3.367-2.446a1 1 0 00-1.176 0l-3.367 2.446c-.784.57-1.838-.196-1.539-1.118l1.286-3.958a1 1 0 00-.364-1.118L2.343 9.385c-.783-.57-.38-1.81.588-1.81h4.162a1 1 0 00.95-.69l1.286-3.958z" />
              </svg>
            </button>
          ))}
        </div>
      </div>
      <div className="mt-4">
        <label htmlFor="reviewComment" className="block text-sm font-medium text-gray-700">
          Comment
        </label>
        <textarea
          id="reviewComment"
          rows={3}
          maxLength={10000}
          value={comment}
          onChange={(event) => onCommentChange(event.target.value)}
          placeholder="What did you think of this product?"
          className={inputClass}
        />
      </div>
      <div className="mt-4 flex items-center gap-2">
        <button
          type="submit"
          disabled={submitting}
          className="rounded-md bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700 disabled:opacity-60"
        >
          {submitting ? 'Saving...' : isEdit ? 'Update review' : 'Submit review'}
        </button>
        {onCancel ? (
          <button
            type="button"
            onClick={onCancel}
            className="rounded-md border border-gray-300 px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50"
          >
            Cancel
          </button>
        ) : null}
      </div>
    </form>
  )
}

export default function ReviewsSection({ productId }) {
  const { user } = useAuth()
  const [reviews, setReviews] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [reload, setReload] = useState(0)

  const [rating, setRating] = useState(0)
  const [comment, setComment] = useState('')
  const [showForm, setShowForm] = useState(false)
  const [submitting, setSubmitting] = useState(false)
  const [formError, setFormError] = useState(null)

  const canReview = Boolean(user && user.role === 'USER')

  const myReview = useMemo(
    () => (user ? reviews.find((review) => review.userId === user.id) ?? null : null),
    [reviews, user],
  )

  const load = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      setReviews(await getProductReviews(productId))
    } catch (err) {
      setError(err.response?.data?.detail || 'Unable to load reviews. Please try again.')
    } finally {
      setLoading(false)
    }
  }, [productId])

  useEffect(() => {
    load()
  }, [load, reload])

  useEffect(() => {
    if (myReview) {
      setRating(myReview.rating)
      setComment(myReview.comment || '')
      setShowForm(false)
    } else {
      setRating(0)
      setComment('')
      setShowForm(canReview)
    }
  }, [myReview, canReview])

  const handleEdit = () => {
    if (!myReview) return
    setRating(myReview.rating)
    setComment(myReview.comment || '')
    setFormError(null)
    setShowForm(true)
  }

  const handleCancel = () => {
    if (myReview) {
      setRating(myReview.rating)
      setComment(myReview.comment || '')
    } else {
      setRating(0)
      setComment('')
    }
    setFormError(null)
    setShowForm(false)
  }

  const handleSubmit = async (event) => {
    event.preventDefault()
    if (!rating || rating < 1 || rating > 5) {
      setFormError('Please choose a rating between 1 and 5.')
      return
    }
    setSubmitting(true)
    setFormError(null)
    try {
      const payload = { rating, comment: comment.trim() || null }
      if (myReview) {
        await updateReview(myReview.id, payload)
      } else {
        await createReview(productId, payload)
      }
      setReload((count) => count + 1)
    } catch (err) {
      setFormError(err.response?.data?.detail || 'Unable to save your review. Please try again.')
    } finally {
      setSubmitting(false)
    }
  }

  const handleDelete = async () => {
    if (!myReview) return
    if (!window.confirm('Delete your review for this product?')) return
    setSubmitting(true)
    setFormError(null)
    try {
      await deleteReview(myReview.id)
      setReload((count) => count + 1)
    } catch (err) {
      setFormError(err.response?.data?.detail || 'Unable to delete your review. Please try again.')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <section className="mt-10">
      <div className="flex items-center justify-between">
        <h2 className="text-xl font-bold text-gray-900">Reviews</h2>
        {!loading && !error ? (
          <p className="text-sm text-gray-500">
            {reviews.length} review{reviews.length === 1 ? '' : 's'}
          </p>
        ) : null}
      </div>

      <div className="mt-4 space-y-4">
        {loading ? (
          <LoadingSpinner label="Loading reviews..." />
        ) : error ? (
          <ErrorState message={error} onRetry={() => setReload((count) => count + 1)} />
        ) : reviews.length === 0 ? (
          <EmptyState
            title="No reviews yet"
            message="Be the first to share your experience with this product."
          />
        ) : (
          reviews.map((review) => (
            <article key={review.id} className="rounded-lg border border-gray-200 bg-white p-4">
              <div className="flex flex-wrap items-center justify-between gap-2">
                <div className="flex items-center gap-2">
                  <StarRating rating={review.rating} />
                  <span className="text-sm font-semibold text-gray-900">{review.userName}</span>
                </div>
                <time dateTime={review.createdAt} className="text-xs text-gray-400">
                  {formatDate(review.createdAt)}
                </time>
              </div>
              {review.comment ? (
                <p className="mt-2 whitespace-pre-line text-sm leading-relaxed text-gray-700">
                  {review.comment}
                </p>
              ) : null}
              {user && review.userId === user.id ? (
                <p className="mt-2 text-xs font-medium text-indigo-600">Your review</p>
              ) : null}
            </article>
          ))
        )}
      </div>

      {canReview ? (
        <div className="mt-6">
          {myReview && !showForm ? (
            <div className="rounded-lg border border-gray-200 bg-indigo-50 p-4">
              <div className="flex flex-wrap items-center justify-between gap-3">
                <div className="flex items-start gap-3">
                  <StarRating rating={myReview.rating} sizeClass="h-5 w-5" />
                  <p className="text-sm text-gray-700">
                    {myReview.comment ? myReview.comment : 'No comment.'}
                  </p>
                </div>
                <div className="flex items-center gap-2">
                  <button
                    type="button"
                    onClick={handleEdit}
                    className="rounded-md border border-gray-300 bg-white px-3 py-1.5 text-sm font-medium text-gray-700 hover:bg-gray-50"
                  >
                    Edit
                  </button>
                  <button
                    type="button"
                    onClick={handleDelete}
                    disabled={submitting}
                    className="rounded-md border border-red-300 bg-white px-3 py-1.5 text-sm font-medium text-red-600 hover:bg-red-50 disabled:opacity-60"
                  >
                    Delete
                  </button>
                </div>
              </div>
            </div>
          ) : null}

          {showForm ? (
            <ReviewForm
              rating={rating}
              comment={comment}
              onRatingChange={setRating}
              onCommentChange={setComment}
              onSubmit={handleSubmit}
              onCancel={myReview ? handleCancel : null}
              submitting={submitting}
              isEdit={Boolean(myReview)}
              error={formError}
            />
          ) : null}
        </div>
      ) : null}
    </section>
  )
}
