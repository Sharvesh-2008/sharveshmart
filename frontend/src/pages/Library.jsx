import { useCallback, useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { listLibrary, getDownloadAuthorization } from '../services/libraryService'
import LoadingSpinner from '../components/ui/LoadingSpinner'
import ErrorState from '../components/ui/ErrorState'
import EmptyState from '../components/ui/EmptyState'

function formatDate(value) {
  if (!value) {
    return '—'
  }
  return new Date(value).toLocaleString(undefined, { dateStyle: 'medium', timeStyle: 'short' })
}

function formatBytes(bytes) {
  const value = Number(bytes) || 0
  if (value < 1024) {
    return `${value} B`
  }
  if (value < 1024 * 1024) {
    return `${(value / 1024).toFixed(1)} KB`
  }
  return `${(value / (1024 * 1024)).toFixed(1)} MB`
}

export default function Library() {
  const [items, setItems] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [reload, setReload] = useState(0)
  const [downloadingId, setDownloadingId] = useState(null)
  const [metadata, setMetadata] = useState(null)
  const [downloadError, setDownloadError] = useState(null)

  const load = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      setItems(await listLibrary())
    } catch (err) {
      setError(err.response?.data?.detail || 'Unable to load your digital library.')
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    load()
  }, [load, reload])

  const handleDownload = async (item) => {
    setDownloadingId(item.productId)
    setMetadata(null)
    setDownloadError(null)
    try {
      setMetadata(await getDownloadAuthorization(item.productId))
    } catch (err) {
      const status = err.response?.status
      if (status === 403) {
        setDownloadError('You are not entitled to download this product.')
      } else if (status === 401) {
        setDownloadError('Your session has expired. Please sign in again.')
      } else {
        setDownloadError(err.response?.data?.detail || 'Unable to authorize download. Please try again.')
      }
    } finally {
      setDownloadingId(null)
    }
  }

  return (
    <div>
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-gray-900">Digital Library</h1>
        <p className="text-sm text-gray-500">{!loading && !error ? `${items.length} products` : ''}</p>
      </div>
      <p className="mt-1 text-sm text-gray-500">
        Download the digital products you have purchased.
      </p>

      <div className="mt-6">
        {loading ? (
          <LoadingSpinner label="Loading your library..." />
        ) : error ? (
          <ErrorState message={error} onRetry={() => setReload((count) => count + 1)} />
        ) : items.length === 0 ? (
          <EmptyState
            title="Your library is empty"
            message="Products you purchase will appear here and be available for download."
          >
            <Link
              to="/products"
              className="rounded-md bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700"
            >
              Browse products
            </Link>
          </EmptyState>
        ) : (
          <div className="space-y-4">
            {items.map((item) => (
              <div
                key={item.id}
                className="rounded-lg border border-gray-200 bg-white p-5 shadow-sm"
              >
                <div className="flex flex-wrap items-center justify-between gap-4">
                  <div>
                    <h2 className="text-base font-semibold text-gray-900">{item.productTitle}</h2>
                    <p className="mt-1 text-sm text-gray-500">
                      Added {formatDate(item.grantedAt)} · Order{' '}
                      <Link
                        to={`/orders/${item.orderId}`}
                        className="text-indigo-600 hover:text-indigo-700"
                      >
                        #{item.orderId}
                      </Link>
                    </p>
                  </div>
                  <button
                    type="button"
                    onClick={() => handleDownload(item)}
                    disabled={downloadingId === item.productId}
                    className="rounded-md bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700 disabled:cursor-not-allowed disabled:opacity-60"
                  >
                    {downloadingId === item.productId ? 'Authorizing...' : 'Download'}
                  </button>
                </div>

                {metadata && metadata.productId === item.productId ? (
                  <div className="mt-4 rounded-md border border-indigo-100 bg-indigo-50 px-4 py-3">
                    <p className="text-sm font-medium text-indigo-800">Download authorized</p>
                    <dl className="mt-2 grid gap-2 text-sm text-indigo-700 sm:grid-cols-2 lg:grid-cols-4">
                      <div>
                        <dt className="font-medium">File name</dt>
                        <dd className="break-all">{metadata.fileName}</dd>
                      </div>
                      <div>
                        <dt className="font-medium">File type</dt>
                        <dd>{metadata.fileType || '—'}</dd>
                      </div>
                      <div>
                        <dt className="font-medium">File size</dt>
                        <dd>{formatBytes(metadata.fileSize)}</dd>
                      </div>
                      <div>
                        <dt className="font-medium">Product</dt>
                        <dd>{metadata.productTitle}</dd>
                      </div>
                    </dl>
                  </div>
                ) : null}
              </div>
            ))}

            {downloadError ? (
              <div className="rounded-md border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
                {downloadError}
              </div>
            ) : null}
          </div>
        )}
      </div>
    </div>
  )
}
