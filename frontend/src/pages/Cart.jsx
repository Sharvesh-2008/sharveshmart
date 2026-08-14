import { useCallback, useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { getCart, removeCartItem, updateCartItem } from '../services/cartService'
import { formatPrice } from '../utils/format'
import LoadingSpinner from '../components/ui/LoadingSpinner'
import ErrorState from '../components/ui/ErrorState'
import EmptyState from '../components/ui/EmptyState'

export default function Cart() {
  const navigate = useNavigate()
  const [cart, setCart] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [reload, setReload] = useState(0)
  const [busyId, setBusyId] = useState(null)

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

  const handleQuantityChange = async (item, nextQuantity) => {
    const quantity = Number(nextQuantity)
    if (!Number.isInteger(quantity) || quantity < 1) return
    setBusyId(item.productId)
    try {
      const updated = await updateCartItem(item.productId, quantity)
      setCart((current) => ({
        ...current,
        items: current.items.map((entry) => (entry.productId === updated.productId ? updated : entry)),
      }))
    } catch (err) {
      setError(err.response?.data?.detail || 'Unable to update quantity. Please try again.')
      setReload((count) => count + 1)
    } finally {
      setBusyId(null)
    }
  }

  const handleRemove = async (productId) => {
    setBusyId(productId)
    try {
      await removeCartItem(productId)
      setCart((current) => ({
        ...current,
        items: current.items.filter((entry) => entry.productId !== productId),
      }))
    } catch (err) {
      setError(err.response?.data?.detail || 'Unable to remove this item. Please try again.')
    } finally {
      setBusyId(null)
    }
  }

  if (loading) {
    return <LoadingSpinner label="Loading your cart..." />
  }

  if (error) {
    return <ErrorState message={error} onRetry={() => setReload((count) => count + 1)} />
  }

  if (!cart || cart.items.length === 0) {
    return (
      <EmptyState title="Your cart is empty" message="Browse the catalog and add digital products you would like to buy.">
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
      <h1 className="text-2xl font-bold text-gray-900">Your Cart</h1>
      <p className="mt-1 text-sm text-gray-500">Review the items you are ready to buy.</p>

      <div className="mt-6 space-y-4">
        {cart.items.map((item) => (
          <div key={item.productId} className="rounded-lg border border-gray-200 bg-white p-5 shadow-sm">
            <div className="flex flex-wrap items-center justify-between gap-4">
              <div className="min-w-0">
                <h3 className="text-base font-semibold text-gray-900">{item.productTitle}</h3>
                <p className="mt-1 text-sm text-gray-500">Unit price: {formatPrice(item.unitPrice)}</p>
              </div>
              <div className="flex flex-wrap items-center gap-4">
                <div className="flex items-center gap-2">
                  <label htmlFor={`quantity-${item.productId}`} className="text-sm font-medium text-gray-600">
                    Qty
                  </label>
                  <input
                    id={`quantity-${item.productId}`}
                    type="number"
                    min={1}
                    defaultValue={item.quantity}
                    key={`${item.productId}-${item.quantity}`}
                    disabled={busyId === item.productId}
                    onBlur={(event) => handleQuantityChange(item, event.target.value)}
                    onKeyDown={(event) => {
                      if (event.key === 'Enter') event.currentTarget.blur()
                    }}
                    className="w-20 rounded-md border border-gray-300 px-2 py-1.5 text-sm text-gray-900 focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500 disabled:opacity-60"
                  />
                </div>
                <span className="w-28 text-right text-base font-semibold text-gray-900">
                  {formatPrice(Number(item.unitPrice) * item.quantity)}
                </span>
                <button
                  type="button"
                  onClick={() => handleRemove(item.productId)}
                  disabled={busyId === item.productId}
                  className="rounded-md border border-red-200 px-3 py-1.5 text-sm font-medium text-red-600 hover:bg-red-50 disabled:opacity-60"
                >
                  Remove
                </button>
              </div>
            </div>
          </div>
        ))}

        <div className="rounded-lg border border-gray-200 bg-white p-5 shadow-sm">
          <div className="flex flex-wrap items-center justify-between gap-4">
            <span className="text-base font-medium text-gray-700">Grand total</span>
            <span className="text-2xl font-bold text-indigo-600">{formatPrice(grandTotal)}</span>
          </div>
          <button
            type="button"
            onClick={() => navigate('/checkout')}
            className="mt-4 w-full rounded-md bg-indigo-600 px-4 py-2.5 text-sm font-medium text-white hover:bg-indigo-700 sm:w-auto"
          >
            Proceed to checkout
          </button>
        </div>
      </div>
    </div>
  )
}
