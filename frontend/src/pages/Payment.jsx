import { useCallback, useEffect, useState } from 'react'
import { Link, useLocation, useParams } from 'react-router-dom'
import { getOrder, payOrder } from '../services/cartService'
import { formatPrice } from '../utils/format'
import LoadingSpinner from '../components/ui/LoadingSpinner'
import ErrorState from '../components/ui/ErrorState'

export default function Payment() {
  const { orderId } = useParams()
  const location = useLocation()
  const stateOrder = location.state?.order

  const [order, setOrder] = useState(stateOrder ?? null)
  const [loading, setLoading] = useState(!stateOrder)
  const [error, setError] = useState(null)
  const [paying, setPaying] = useState(false)
  const [payment, setPayment] = useState(null)
  const [payError, setPayError] = useState(null)

  const targetOrderId = orderId ?? stateOrder?.id

  const load = useCallback(async () => {
    if (!targetOrderId) return
    setLoading(true)
    setError(null)
    try {
      setOrder(await getOrder(targetOrderId))
    } catch (err) {
      setError(err.response?.data?.detail || 'Unable to load this order.')
    } finally {
      setLoading(false)
    }
  }, [targetOrderId])

  useEffect(() => {
    if (!stateOrder) {
      load()
    }
  }, [load, stateOrder])

  const handlePay = async () => {
    if (!targetOrderId) return
    setPaying(true)
    setPayError(null)
    try {
      const result = await payOrder(targetOrderId)
      setPayment(result)
      setOrder((current) => (current ? { ...current, status: 'PAID' } : current))
    } catch (err) {
      const message = err.response?.data?.detail || 'Payment failed. Please try again.'
      if (message.toLowerCase().includes('only pending orders')) {
        setOrder((current) => (current ? { ...current, status: 'PAID' } : current))
      } else {
        setPayError(message)
      }
    } finally {
      setPaying(false)
    }
  }

  if (loading) {
    return <LoadingSpinner label="Loading order..." />
  }

  if (error) {
    return <ErrorState message={error} onRetry={load} />
  }

  if (!order) {
    return (
      <ErrorState
        title="Order not found"
        message="We could not find an order to complete payment for. Please try again from your cart."
      />
    )
  }

  const isPaid = order.status === 'PAID' || payment?.status === 'SUCCESS'
  const isFailed = order.status === 'FAILED' || payment?.status === 'FAILED'

  if (isPaid) {
    return (
      <div className="rounded-lg border border-gray-200 bg-white p-6 text-center shadow-sm sm:p-10">
        <span className="mx-auto flex h-12 w-12 items-center justify-center rounded-full bg-green-100 text-2xl font-bold text-green-600">
          ✓
        </span>
        <h1 className="mt-4 text-2xl font-bold text-gray-900">Payment successful</h1>
        <p className="mt-2 text-sm text-gray-500">
          Order #{order.id} was paid for {formatPrice(order.totalAmount)}.
        </p>
        {payment?.providerReference ? (
          <p className="mt-1 text-sm text-gray-500">Provider reference: {payment.providerReference}</p>
        ) : null}
        <div className="mt-6 flex flex-wrap justify-center gap-3">
          <Link
            to="/orders"
            className="rounded-md bg-indigo-600 px-5 py-2.5 text-sm font-medium text-white hover:bg-indigo-700"
          >
            View your orders
          </Link>
          <Link
            to="/library"
            className="rounded-md border border-gray-300 px-5 py-2.5 text-sm font-medium text-gray-700 hover:bg-gray-50"
          >
            Go to Digital Library
          </Link>
        </div>
      </div>
    )
  }

  if (isFailed) {
    return (
      <div className="space-y-4">
        <ErrorState
          title="Payment failed"
          message="Your payment could not be completed and this order was marked as failed. You can place a new order and try again."
        />
        <div className="flex justify-center">
          <Link
            to="/products"
            className="rounded-md border border-gray-300 px-5 py-2.5 text-sm font-medium text-gray-700 hover:bg-gray-50"
          >
            Back to products
          </Link>
        </div>
      </div>
    )
  }

  return (
    <div>
      <h1 className="text-2xl font-bold text-gray-900">Payment</h1>
      <p className="mt-1 text-sm text-gray-500">Complete payment for your pending order.</p>

      <div className="mt-6 space-y-4">
        <div className="rounded-lg border border-gray-200 bg-white p-5 shadow-sm">
          <div className="flex flex-wrap items-center justify-between gap-4">
            <div>
              <p className="text-sm text-gray-500">Order #{order.id}</p>
              <p className="mt-0.5 text-sm text-gray-500">Status: {order.status}</p>
            </div>
            <span className="text-2xl font-bold text-indigo-600">{formatPrice(order.totalAmount)}</span>
          </div>
        </div>

        {payError ? (
          <div className="rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
            {payError}
          </div>
        ) : null}

        <div className="rounded-lg border border-gray-200 bg-white p-5 shadow-sm">
          <p className="text-sm text-gray-500">
            A mock payment provider is used for this project. No real money will be charged.
          </p>
          <div className="mt-4 flex flex-wrap gap-3">
            <button
              type="button"
              onClick={handlePay}
              disabled={paying}
              className="rounded-md bg-indigo-600 px-5 py-2.5 text-sm font-medium text-white hover:bg-indigo-700 disabled:opacity-60"
            >
              {paying ? 'Processing...' : 'Pay now'}
            </button>
            <Link
              to="/products"
              className="rounded-md border border-gray-300 px-5 py-2.5 text-sm font-medium text-gray-700 hover:bg-gray-50"
            >
              Back to products
            </Link>
          </div>
        </div>
      </div>
    </div>
  )
}
