import { useCallback, useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { checkout, getCart } from '../services/cartService'
import { formatPrice } from '../utils/format'
import LoadingSpinner from '../components/ui/LoadingSpinner'
import ErrorState from '../components/ui/ErrorState'
import EmptyState from '../components/ui/EmptyState'

export default function Checkout() {
  const navigate = useNavigate()
  const [cart, setCart] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [reload, setReload] = useState(0)
  const [submitting, setSubmitting] = useState(false)
  const [submitError, setSubmitError] = useState(null)

  const load = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      setCart(await getCart())
    } catch (err) {
      setError(err.response?.data?.detail || 'Unable to load your cart. Please try again.')
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    load()
  }, [load, reload])

  const handleConfirm = async () => {
    setSubmitting(true)
    setSubmitError(null)
    try {
      const order = await checkout()
      navigate('/payment', { state: { order } })
    } catch (err) {
      setSubmitError(err.response?.data?.detail || 'Unable to place your order. Please try again.')
      setSubmitting(false)
    }
  }

  if (loading) {
    return <LoadingSpinner label="Preparing checkout..." />
  }

  if (error) {
    return <ErrorState message={error} onRetry={() => setReload((count) => count + 1)} />
  }

  if (!cart || cart.items.length === 0) {
    return (
      <EmptyState title="Nothing to check out" message="Your cart is empty, so there is nothing to order yet.">
        <Link
          to="/products"
          className="rounded-md bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700"
        >
          Browse products
        </Link>
      </EmptyState>
    )
  }

  const grandTotal = cart.items.reduce(
    (sum, item) => sum + Number(item.unitPrice) * item.quantity,
    0,
  )

  return (
    <div>
      <h1 className="text-2xl font-bold text-gray-900">Checkout</h1>
      <p className="mt-1 text-sm text-gray-500">Confirm the details below to place your order.</p>

      <div className="mt-6 space-y-4">
        <div className="rounded-lg border border-gray-200 bg-white p-5 shadow-sm">
          <h2 className="text-sm font-medium text-gray-500">Order summary</h2>
          <ul className="mt-3 divide-y divide-gray-100">
            {cart.items.map((item) => (
              <li key={item.productId} className="flex items-center justify-between gap-4 py-3">
                <div className="min-w-0">
                  <p className="truncate text-sm font-medium text-gray-900">{item.productTitle}</p>
                  <p className="mt-0.5 text-sm text-gray-500">
                    {formatPrice(item.unitPrice)} × {item.quantity}
                  </p>
                </div>
                <span className="text-sm font-semibold text-gray-900">
                  {formatPrice(Number(item.unitPrice) * item.quantity)}
                </span>
              </li>
            ))}
          </ul>
          <div className="mt-3 flex items-center justify-between border-t border-gray-100 pt-4">
            <span className="text-base font-medium text-gray-700">Order total</span>
            <span className="text-2xl font-bold text-indigo-600">{formatPrice(grandTotal)}</span>
          </div>
        </div>

        {submitError ? (
          <div className="rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
            {submitError}
          </div>
        ) : null}

        <div className="rounded-lg border border-gray-200 bg-white p-5 shadow-sm">
          <p className="text-sm text-gray-500">
            Confirming will create a pending order and empty your cart. You will complete payment on the next step.
          </p>
          <div className="mt-4 flex flex-wrap gap-3">
            <button
              type="button"
              onClick={handleConfirm}
              disabled={submitting}
              className="rounded-md bg-indigo-600 px-5 py-2.5 text-sm font-medium text-white hover:bg-indigo-700 disabled:opacity-60"
            >
              {submitting ? 'Placing order...' : 'Confirm order'}
            </button>
            <Link
              to="/cart"
              className="rounded-md border border-gray-300 px-5 py-2.5 text-sm font-medium text-gray-700 hover:bg-gray-50"
            >
              Back to cart
            </Link>
          </div>
        </div>
      </div>
    </div>
  )
}
