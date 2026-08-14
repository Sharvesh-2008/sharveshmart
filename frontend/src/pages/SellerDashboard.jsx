import { useCallback, useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../hooks/useAuth'
import { archiveProduct, getSellerProducts, submitProduct } from '../services/sellerProductService'
import { formatPrice } from '../utils/format'
import LoadingSpinner from '../components/ui/LoadingSpinner'
import ErrorState from '../components/ui/ErrorState'
import EmptyState from '../components/ui/EmptyState'

const STATUS_BADGES = {
  DRAFT: { label: 'Draft', className: 'bg-gray-100 text-gray-700' },
  PENDING_APPROVAL: { label: 'Pending approval', className: 'bg-amber-100 text-amber-700' },
  APPROVED: { label: 'Approved', className: 'bg-green-100 text-green-700' },
  REJECTED: { label: 'Rejected', className: 'bg-red-100 text-red-700' },
  ARCHIVED: { label: 'Archived', className: 'bg-gray-200 text-gray-600' },
}

export default function SellerDashboard() {
  const { user } = useAuth()
  const [products, setProducts] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [reload, setReload] = useState(0)
  const [busyId, setBusyId] = useState(null)
  const [actionError, setActionError] = useState(null)

  const load = useCallback(async () => {
    if (!user) return
    setLoading(true)
    setError(null)
    try {
      const items = await getSellerProducts(user.id)
      setProducts([...items].sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt)))
    } catch (err) {
      setError(err.response?.data?.detail || 'Unable to load your products.')
    } finally {
      setLoading(false)
    }
  }, [user])

  useEffect(() => {
    load()
  }, [load, reload])

  if (!user) {
    return <LoadingSpinner label="Loading account..." />
  }

  const handleArchive = async (product) => {
    if (!window.confirm(`Archive "${product.title}"? It will no longer be sold.`)) return
    setBusyId(product.id)
    setActionError(null)
    try {
      await archiveProduct(product.id)
      setReload((count) => count + 1)
    } catch (err) {
      setActionError(err.response?.data?.detail || 'Unable to archive this product.')
    } finally {
      setBusyId(null)
    }
  }

  const handleSubmit = async (product) => {
    setBusyId(product.id)
    setActionError(null)
    try {
      await submitProduct(product.id)
      setReload((count) => count + 1)
    } catch (err) {
      setActionError(err.response?.data?.detail || 'Unable to submit this product.')
    } finally {
      setBusyId(null)
    }
  }

  return (
    <div>
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">My Products</h1>
          <p className="mt-1 text-sm text-gray-500">
            Manage your digital products and track their approval status.
          </p>
        </div>
        <Link
          to="/seller/products/new"
          className="rounded-md bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700"
        >
          New product
        </Link>
      </div>

      {actionError ? (
        <div className="mt-4">
          <ErrorState message={actionError} />
        </div>
      ) : null}

      <div className="mt-6">
        {loading ? (
          <LoadingSpinner label="Loading your products..." />
        ) : error ? (
          <ErrorState message={error} onRetry={() => setReload((count) => count + 1)} />
        ) : products.length === 0 ? (
          <EmptyState
            title="You have no products yet"
            message="Create your first product and submit it for approval to start selling."
          >
            <Link
              to="/seller/products/new"
              className="rounded-md bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700"
            >
              Create a product
            </Link>
          </EmptyState>
        ) : (
          <div className="overflow-hidden rounded-lg border border-gray-200 bg-white shadow-sm">
            <table className="min-w-full divide-y divide-gray-200 text-sm">
              <thead className="bg-gray-50 text-left text-xs font-medium uppercase tracking-wide text-gray-500">
                <tr>
                  <th className="px-4 py-3">Product</th>
                  <th className="px-4 py-3">Status</th>
                  <th className="px-4 py-3">Price</th>
                  <th className="px-4 py-3 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200">
                {products.map((product) => {
                  const badge =
                    STATUS_BADGES[product.status] ?? {
                      label: product.status,
                      className: 'bg-gray-100 text-gray-700',
                    }
                  return (
                    <tr key={product.id}>
                      <td className="px-4 py-3">
                        <p className="font-medium text-gray-900">{product.title}</p>
                        <p className="mt-0.5 text-xs text-gray-500">{product.categoryName}</p>
                        {product.status === 'REJECTED' ? (
                          <p className="mt-1 text-xs font-medium text-red-600">
                            Rejected. Edit your product and resubmit it for approval.
                          </p>
                        ) : null}
                        {product.status === 'PENDING_APPROVAL' ? (
                          <p className="mt-1 text-xs font-medium text-amber-600">
                            Waiting for an admin to review it.
                          </p>
                        ) : null}
                      </td>
                      <td className="px-4 py-3">
                        <span
                          className={`inline-block rounded-full px-2.5 py-0.5 text-xs font-medium ${badge.className}`}
                        >
                          {badge.label}
                        </span>
                      </td>
                      <td className="px-4 py-3 font-medium text-gray-900">{formatPrice(product.price)}</td>
                      <td className="px-4 py-3">
                        <div className="flex items-center justify-end gap-2">
                          <Link
                            to={`/seller/products/${product.id}/edit`}
                            className="rounded-md border border-gray-300 px-3 py-1.5 text-xs font-medium text-gray-700 hover:bg-gray-50"
                          >
                            Edit
                          </Link>
                          {product.status === 'DRAFT' || product.status === 'REJECTED' ? (
                            <button
                              type="button"
                              disabled={busyId === product.id}
                              onClick={() => handleSubmit(product)}
                              className="rounded-md bg-indigo-600 px-3 py-1.5 text-xs font-medium text-white hover:bg-indigo-700 disabled:opacity-60"
                            >
                              Submit for approval
                            </button>
                          ) : null}
                          {product.status !== 'ARCHIVED' ? (
                            <button
                              type="button"
                              disabled={busyId === product.id}
                              onClick={() => handleArchive(product)}
                              className="rounded-md border border-red-300 px-3 py-1.5 text-xs font-medium text-red-600 hover:bg-red-50 disabled:opacity-60"
                            >
                              Archive
                            </button>
                          ) : null}
                        </div>
                      </td>
                    </tr>
                  )
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  )
}
