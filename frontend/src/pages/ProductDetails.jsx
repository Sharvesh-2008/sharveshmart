import { useCallback, useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { getProduct } from '../services/products'
import { formatPrice } from '../utils/format'
import LoadingSpinner from '../components/ui/LoadingSpinner'
import ErrorState from '../components/ui/ErrorState'
import EmptyState from '../components/ui/EmptyState'
import AddToCartButton from '../components/cart/AddToCartButton'
import ReviewsSection from '../components/reviews/ReviewsSection'

export default function ProductDetails() {
  const { productId } = useParams()
  const [product, setProduct] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [notFound, setNotFound] = useState(false)

  const load = useCallback(async () => {
    setLoading(true)
    setError(null)
    setNotFound(false)
    try {
      setProduct(await getProduct(productId))
    } catch (err) {
      if (err.response?.status === 404) {
        setNotFound(true)
      } else {
        setError(err.response?.data?.detail || 'Unable to load this product.')
      }
    } finally {
      setLoading(false)
    }
  }, [productId])

  useEffect(() => {
    load()
  }, [load])

  if (loading) {
    return <LoadingSpinner label="Loading product..." />
  }

  if (error) {
    return <ErrorState message={error} onRetry={load} />
  }

  if (notFound || !product) {
    return (
      <EmptyState
        title="Product not found"
        message="This product does not exist or is not available for purchase."
      >
        <Link
          to="/products"
          className="rounded-md bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700"
        >
          Back to products
        </Link>
      </EmptyState>
    )
  }

  return (
    <div>
      <nav className="text-sm text-gray-500">
        <Link to="/products" className="hover:text-indigo-600">
          Products
        </Link>
        <span className="mx-2">/</span>
        <span className="text-gray-700">{product.title}</span>
      </nav>

      <div className="mt-4 rounded-lg border border-gray-200 bg-white p-6 shadow-sm sm:p-8">
        <div className="flex flex-wrap items-start justify-between gap-4">
          <h1 className="text-2xl font-bold text-gray-900">{product.title}</h1>
          <div className="flex items-center gap-4">
            <span className="text-2xl font-bold text-indigo-600">{formatPrice(product.price)}</span>
            <AddToCartButton product={product} />
          </div>
        </div>

        <dl className="mt-6 grid gap-4 sm:grid-cols-2">
          <div>
            <dt className="text-sm font-medium text-gray-500">Category</dt>
            <dd className="mt-1 text-sm text-gray-900">{product.categoryName}</dd>
          </div>
          <div>
            <dt className="text-sm font-medium text-gray-500">Seller</dt>
            <dd className="mt-1 text-sm text-gray-900">{product.sellerName}</dd>
          </div>
        </dl>

        <div className="mt-6">
          <h2 className="text-sm font-medium text-gray-500">Description</h2>
          <p className="mt-1 whitespace-pre-line text-sm leading-relaxed text-gray-700">
            {product.description || 'No description provided.'}
          </p>
        </div>
      </div>

      <ReviewsSection productId={product.id} />
    </div>
  )
}