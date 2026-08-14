import { useCallback, useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { getOrder, payOrder } from '../services/orderService'
import { formatPrice } from '../utils/format'
import LoadingSpinner from '../components/ui/LoadingSpinner'
import ErrorState from '../components/ui/ErrorState'
import EmptyState from '../components/ui/EmptyState'

const STATUS_STYLES = {
  PENDING: 'bg-amber-100 text-amber-800',
  PAID: 'bg-green-100 text-green-800',
  FAILED: 'bg-red-100 text-red-800',
  CANCELLED: 'bg-gray-100 text-gray-600',
}

function StatusBadge({ status }) {
  return (
    <span
      className={`inline-flex rounded-full px-2.5 py-0.5 text-xs font-medium ${
        STATUS_STYLES[status] ?? 'bg-gray-100 text-gray-600'
      }`}
    >
      {status}
    </span>
  )
}

function formatDate(value) {
  if (!value) {
    return '—'
  }
  return new Date(value).toLocaleString(undefined, { dateStyle: 'medium', timeStyle: 'short' })
}

export default function OrderDetails() {
  const { orderId } = useParams()
  const [order, setOrder] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [notFound, setNotFound] = useState(false)
  const [paying, setPaying] = useState(false)
  const [payResult, setPayResult] = useState(null)
  const [payError, setPayError] = useState(null)

  const load = useCallback(async () => {
    setLoading(true)
    setError(null)
    setNotFound(false)
    try {
      setOrder(await getOrder(orderId))
    } catch (err) {
      if (err.response?.status === 404) {
        setNotFound(true)
      } else {
        setError(err.response?.data?.detail || 'Unable to load this order.')
      }
    } finally {
      setLoading(false)
    }
  }, [orderId])

  useEffect(() => {
    load()
  }, [load])

  const handlePay = async () => {
    setPaying(true)
    setPayResult(null)
    setPayError(null)
    try {
      setPayResult(await payOrder(orderId))
      await load()
    } catch (err) {
      setPayError(err.response?.data?.detail || 'Payment failed. Please try again.')
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

  if (notFound || !order) {
    return (
      <EmptyState title="Order not found" message="This order does not exist or is not available to you.">
        <Link
          to="/orders"
          className="rounded-md bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700"
        >
          Back to orders
        </Link>
      </EmptyState>
    )
  }

  return (
    <div>
      <nav className="text-sm text-gray-500">
        <Link to="/orders" className="hover:text-indigo-600">
          Orders
        </Link>
        <span className="mx-2">/</span>
        <span className="text-gray-700">Order #{order.id}</span>
      </nav>

      <div className="mt-4 rounded-lg border border-gray-200 bg-white p-6 shadow-sm sm:p-8">
        <div className="flex flex-wrap items-start justify-between gap-4">
          <div>
            <h1 className="text-2xl font-bold text-gray-900">Order #{order.id}</h1>
            <p className="mt-1 text-sm text-gray-500">Placed on {formatDate(order.createdAt)}</p>
          </div>
          <StatusBadge status={order.status} />
        </div>

        <div className="mt-6 overflow-x-auto">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-4 py-3 text-left text-xs font-medium uppercase tracking-wide text-gray-500">
                  Product
                </th>
                <th className="px-4 py-3 text-right text-xs font-medium uppercase tracking-wide text-gray-500">
                  Unit price
                </th>
                <th className="px-4 py-3 text-right text-xs font-medium uppercase tracking-wide text-gray-500">
                  Quantity
                </th>
                <th className="px-4 py-3 text-right text-xs font-medium uppercase tracking-wide text-gray-500">
                  Total
                </th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-200">
              {order.items.map((item) => (
                <tr key={item.id}>
                  <td className="px-4 py-3 text-sm text-gray-900">{item.productTitle}</td>
                  <td className="px-4 py-3 text-right text-sm text-gray-600">{formatPrice(item.unitPrice)}</td>
                  <td className="px-4 py-3 text-right text-sm text-gray-600">{item.quantity}</td>
                  <td className="px-4 py-3 text-right text-sm font-medium text-gray-900">
                    {formatPrice(item.unitPrice * item.quantity)}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        <div className="mt-4 flex justify-end">
          <dl className="w-full max-w-xs space-y-1">
            <div className="flex items-center justify-between text-sm text-gray-600">
              <dt>Subtotal</dt>
              <dd>{formatPrice(order.totalAmount)}</dd>
            </div>
            <div className="flex items-center justify-between border-t border-gray-200 pt-2 text-base font-semibold text-gray-900">
              <dt>Total</dt>
              <dd>{formatPrice(order.totalAmount)}</dd>
            </div>
          </dl>
        </div>

        <div className="mt-6 border-t border-gray-200 pt-6">
          {order.status === 'PENDING' ? (
            <div>
              <div className="flex flex-wrap items-center justify-between gap-4">
                <p className="text-sm text-gray-600">
                  This order is still pending payment. Complete your payment to unlock your items.
                </p>
                <button
                  type="button"
                  onClick={handlePay}
                  disabled={paying}
                  className="rounded-md bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700 disabled:cursor-not-allowed disabled:opacity-60"
                >
                  {paying ? 'Processing payment...' : 'Pay now'}
                </button>
              </div>
              {payResult ? (
                <div className="mt-4 rounded-md border border-green-200 bg-green-50 px-4 py-3">
                  <p className="text-sm font-medium text-green-800">Payment successful</p>
                  <dl className="mt-2 grid gap-2 text-sm text-green-700 sm:grid-cols-2">
                    <div>
                      <dt className="font-medium">Reference</dt>
                      <dd>{payResult.providerReference || '—'}</dd>
                    </div>
                    <div>
                      <dt className="font-medium">Method</dt>
                      <dd>{payResult.method || '—'}</dd>
                    </div>
                    <div>
                      <dt className="font-medium">Amount</dt>
                      <dd>{formatPrice(payResult.amount)}</dd>
                    </div>
                    <div>
                      <dt className="font-medium">Paid at</dt>
                      <dd>{formatDate(payResult.paidAt)}</dd>
                    </div>
                  </dl>
                </div>
              ) : null}
              {payError ? (
                <div className="mt-4 rounded-md border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
                  {payError}
                </div>
              ) : null}
            </div>
          ) : order.status === 'PAID' ? (
            <div>
              <p className="text-sm text-gray-600">
                This order is paid. Your purchased products are available in your digital library.
              </p>
              <Link
                to="/library"
                className="mt-3 inline-block rounded-md bg-green-600 px-4 py-2 text-sm font-medium text-white hover:bg-green-700"
              >
                Go to your library
              </Link>
            </div>
          ) : (
            <p className="text-sm text-gray-600">
              {order.status === 'FAILED'
                ? 'Payment for this order failed. Please contact support if you need assistance.'
                : 'This order has been cancelled.'}
            </p>
          )}
        </div>
      </div>
    </div>
  )
}
