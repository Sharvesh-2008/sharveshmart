import { useCallback, useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { useAuth } from '../hooks/useAuth'
import { getCategories } from '../services/categories'
import { createProduct, getSellerProducts, submitProduct, updateProduct } from '../services/sellerProductService'
import LoadingSpinner from '../components/ui/LoadingSpinner'
import ErrorState from '../components/ui/ErrorState'

const inputClass =
  'mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 text-sm shadow-sm focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500'

export default function SellerProductForm() {
  const { id } = useParams()
  const productId = id ? Number(id) : null
  const navigate = useNavigate()
  const { user } = useAuth()

  const [categories, setCategories] = useState([])
  const [categoriesError, setCategoriesError] = useState(null)
  const [product, setProduct] = useState(null)
  const [loading, setLoading] = useState(Boolean(productId))
  const [loadingError, setLoadingError] = useState(null)

  const [categoryId, setCategoryId] = useState('')
  const [title, setTitle] = useState('')
  const [description, setDescription] = useState('')
  const [price, setPrice] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState(null)

  const loadCategories = useCallback(async () => {
    try {
      setCategories(await getCategories())
    } catch (err) {
      setCategoriesError(err.response?.data?.detail || 'Unable to load categories.')
    }
  }, [])

  const loadProduct = useCallback(async () => {
    if (!productId || !user) return
    setLoading(true)
    setLoadingError(null)
    try {
      const own = await getSellerProducts(user.id)
      const found = own.find((item) => item.id === productId)
      if (!found) {
        setLoadingError('Product not found or you do not own it.')
        return
      }
      setProduct(found)
      setCategoryId(found.categoryId ? String(found.categoryId) : '')
      setTitle(found.title)
      setDescription(found.description || '')
      setPrice(found.price != null ? String(found.price) : '')
    } catch (err) {
      setLoadingError(err.response?.data?.detail || 'Unable to load this product.')
    } finally {
      setLoading(false)
    }
  }, [productId, user])

  useEffect(() => {
    loadCategories()
  }, [loadCategories])

  useEffect(() => {
    if (productId) {
      loadProduct()
    }
  }, [productId, loadProduct])

  const validate = () => {
    if (!categoryId) return 'Please select a category.'
    if (!title.trim()) return 'Title is required.'
    if (price === '' || Number.isNaN(Number(price)) || Number(price) < 0) {
      return 'Please enter a valid price of 0.00 or more.'
    }
    return null
  }

  const buildPayload = () => ({
    categoryId: Number(categoryId),
    title: title.trim(),
    description: description.trim() || null,
    price: Number(price),
  })

  const handleSubmit = async (event) => {
    event.preventDefault()
    const validationError = validate()
    if (validationError) {
      setError(validationError)
      return
    }
    setSubmitting(true)
    setError(null)
    try {
      const payload = buildPayload()
      if (productId) {
        await updateProduct(productId, payload)
      } else {
        await createProduct(payload)
      }
      navigate('/seller/products')
    } catch (err) {
      setError(err.response?.data?.detail || 'Unable to save this product.')
    } finally {
      setSubmitting(false)
    }
  }

  const handleSaveAndSubmit = async () => {
    const validationError = validate()
    if (validationError) {
      setError(validationError)
      return
    }
    setSubmitting(true)
    setError(null)
    try {
      const payload = buildPayload()
      await updateProduct(productId, payload)
      await submitProduct(productId)
      navigate('/seller/products')
    } catch (err) {
      setError(err.response?.data?.detail || 'Unable to save this product.')
    } finally {
      setSubmitting(false)
    }
  }

  if (!user) {
    return <LoadingSpinner label="Loading account..." />
  }

  if (loading) {
    return <LoadingSpinner label="Loading product..." />
  }

  if (loadingError) {
    return <ErrorState message={loadingError} onRetry={loadProduct} />
  }

  return (
    <div className="mx-auto max-w-2xl">
      <nav className="text-sm text-gray-500">
        <Link to="/seller/products" className="hover:text-indigo-600">
          My Products
        </Link>
        <span className="mx-2">/</span>
        <span className="text-gray-700">{productId ? 'Edit product' : 'New product'}</span>
      </nav>

      <h1 className="mt-3 text-2xl font-bold text-gray-900">
        {productId ? 'Edit product' : 'New product'}
      </h1>
      <p className="mt-1 text-sm text-gray-500">
        {productId
          ? `Current status: ${product ? product.status : ''}. Editing does not change the approval status.`
          : 'New products are saved as drafts and must be submitted for approval before they go on sale.'}
      </p>

      {categoriesError ? (
        <div className="mt-6">
          <ErrorState message={categoriesError} onRetry={loadCategories} />
        </div>
      ) : null}

      <form
        onSubmit={handleSubmit}
        className="mt-6 space-y-4 rounded-lg border border-gray-200 bg-white p-6 shadow-sm"
      >
        {error ? <ErrorState message={error} /> : null}

        <div>
          <label htmlFor="categoryId" className="block text-sm font-medium text-gray-700">
            Category
          </label>
          <select
            id="categoryId"
            value={categoryId}
            onChange={(event) => setCategoryId(event.target.value)}
            required
            className={inputClass}
          >
            <option value="">Select a category</option>
            {categories.map((category) => (
              <option key={category.id} value={category.id}>
                {category.name}
              </option>
            ))}
          </select>
        </div>

        <div>
          <label htmlFor="title" className="block text-sm font-medium text-gray-700">
            Title
          </label>
          <input
            id="title"
            type="text"
            required
            maxLength={200}
            value={title}
            onChange={(event) => setTitle(event.target.value)}
            placeholder="e.g. Premium UI Kit"
            className={inputClass}
          />
        </div>

        <div>
          <label htmlFor="description" className="block text-sm font-medium text-gray-700">
            Description
          </label>
          <textarea
            id="description"
            rows={5}
            maxLength={10000}
            value={description}
            onChange={(event) => setDescription(event.target.value)}
            placeholder="Describe what buyers will receive..."
            className={inputClass}
          />
        </div>

        <div>
          <label htmlFor="price" className="block text-sm font-medium text-gray-700">
            Price (USD)
          </label>
          <input
            id="price"
            type="number"
            required
            min="0"
            step="0.01"
            value={price}
            onChange={(event) => setPrice(event.target.value)}
            placeholder="0.00"
            className={inputClass}
          />
        </div>

        <div className="flex flex-wrap items-center gap-2 pt-1">
          <button
            type="submit"
            disabled={submitting}
            className="rounded-md bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700 disabled:opacity-60"
          >
            {submitting ? 'Saving...' : productId ? 'Save changes' : 'Create product'}
          </button>
          {productId && (product?.status === 'DRAFT' || product?.status === 'REJECTED') ? (
            <button
              type="button"
              disabled={submitting}
              onClick={handleSaveAndSubmit}
              className="rounded-md bg-green-600 px-4 py-2 text-sm font-medium text-white hover:bg-green-700 disabled:opacity-60"
            >
              Save & submit for approval
            </button>
          ) : null}
          <Link
            to="/seller/products"
            className="rounded-md border border-gray-300 px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50"
          >
            Cancel
          </Link>
        </div>
      </form>
    </div>
  )
}
