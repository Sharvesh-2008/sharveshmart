import { useCallback, useEffect, useState } from 'react'
import { listPendingProducts, approveProduct, rejectProduct } from '../services/adminService'
import { formatPrice } from '../utils/format'
import LoadingSpinner from '../components/ui/LoadingSpinner'
import ErrorState from '../components/ui/ErrorState'
import EmptyState from '../components/ui/EmptyState'

function formatDate(value) {
  return new Date(value).toLocaleDateString(undefined, { year: 'numeric', month: 'short', day: 'numeric' })
}

export default function AdminModeration() {
  const [products, setProducts] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [actionId, setActionId] = useState(null)
  const [actionError, setActionError] = useState(null)
  const [reload, setReload] = useState(0)

  const load = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      setProducts(await listPendingProducts())
    } catch (err) {
      setError(err.response?.data?.detail || 'Unable to load pending products.')
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    load()
  }, [load, reload])

  const handleApprove = async (productId) => {
    setActionId(productId)
    setActionError(null)
    try {
      await approveProduct(productId)
      setReload((count) => count + 1)
    } catch (err) {
      setActionError(err.response?.data?.detail || 'Unable to approve this product.')
      setActionId(null)
    }
  }

  const handleReject = async (productId) => {
    setActionId(productId)
    setActionError(null)
    try {
      await rejectProduct(productId)
      setReload((count) => count + 1)
    } catch (err) {
      setActionError(err.response?.data?.detail || 'Unable to reject this product.')
      setActionId(null)
    }
  }

  return (
    <div>
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-gray-900">Product Moderation</h1>
        <p className="text-sm text-gray-500">
          {!loading && !error ? `${products.length} pending` : ''}
        </p>
      </div>
      <p className="mt-1 text-sm text-gray-500">
        Review products submitted by sellers for approval.
      </p>

      <div className="mt-6">
        {loading ? (
          <LoadingSpinner label="Loading pending products..." />
        ) : error ? (
          <ErrorState message={error} onRetry={() => setReload((count) => count + 1)} />
        ) : products.length === 0 ? (
          <EmptyState
            title="Nothing to moderate"
            message="There are no products waiting for approval right now."
          />
        ) : (
          <div className="overflow-hidden rounded-lg border border-gray-200 bg-white shadow-sm">
            {actionError ? (
              <div className="border-b border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
                {actionError}
              </div>
            ) : null}
            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-gray-200 text-sm">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="px-4 py-3 text-left font-medium text-gray-500">Product</th>
                    <th className="px-4 py-3 text-left font-medium text-gray-500">Seller</th>
                    <th className="px-4 py-3 text-left font-medium text-gray-500">Price</th>
                    <th className="px-4 py-3 text-left font-medium text-gray-500">Submitted</th>
                    <th className="px-4 py-3 text-right font-medium text-gray-500">Actions</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-200 bg-white">
                  {products.map((product) => (
                    <tr key={product.id} className={actionId === product.id ? 'opacity-60' : ''}>
                      <td className="px-4 py-4">
                        <p className="font-semibold text-gray-900">{product.title}</p>
                        <p className="text-xs text-gray-500">{product.categoryName}</p>
                      </td>
                      <td className="px-4 py-4 text-gray-700">{product.sellerName}</td>
                      <td className="whitespace-nowrap px-4 py-4 font-medium text-indigo-600">
                        {formatPrice(product.price)}
                      </td>
                      <td className="whitespace-nowrap px-4 py-4 text-gray-500">
                        {formatDate(product.createdAt)}
                      </td>
                      <td className="px-4 py-4">
                        <div className="flex justify-end gap-2">
                          <button
                            type="button"
                            disabled={actionId !== null}
                            onClick={() => handleApprove(product.id)}
                            className="rounded-md bg-indigo-600 px-3 py-1.5 text-sm font-medium text-white hover:bg-indigo-700 disabled:cursor-not-allowed disabled:opacity-50"
                          >
                            Approve
                          </button>
                          <button
                            type="button"
                            disabled={actionId !== null}
                            onClick={() => handleReject(product.id)}
                            className="rounded-md border border-red-300 px-3 py-1.5 text-sm font-medium text-red-600 hover:bg-red-50 disabled:cursor-not-allowed disabled:opacity-50"
                          >
                            Reject
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}
      </div>
    </div>
  )
}
