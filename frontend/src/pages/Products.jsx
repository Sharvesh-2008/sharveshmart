import { useCallback, useEffect, useMemo, useState } from 'react'
import { getProducts, getProductsByCategory } from '../services/products'
import { getCategories } from '../services/categories'
import ProductCard from '../components/products/ProductCard'
import LoadingSpinner from '../components/ui/LoadingSpinner'
import ErrorState from '../components/ui/ErrorState'
import EmptyState from '../components/ui/EmptyState'

export default function Products() {
  const [products, setProducts] = useState([])
  const [categories, setCategories] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [reload, setReload] = useState(0)
  const [search, setSearch] = useState('')
  const [categoryId, setCategoryId] = useState('')
  const [sortBy, setSortBy] = useState('default')

  const load = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const productPromise = categoryId ? getProductsByCategory(Number(categoryId)) : getProducts()
      const [productList, categoryList] = await Promise.all([productPromise, getCategories()])
      setProducts(productList)
      setCategories(categoryList)
    } catch (err) {
      setError(err.response?.data?.detail || 'Unable to load products. Please try again.')
    } finally {
      setLoading(false)
    }
  }, [categoryId])

  useEffect(() => {
    load()
  }, [load, reload])

  const visibleProducts = useMemo(() => {
    const query = search.trim().toLowerCase()
    let list = products
    if (query) {
      list = list.filter(
        (product) =>
          (product.title || '').toLowerCase().includes(query) ||
          (product.description || '').toLowerCase().includes(query),
      )
    }
    switch (sortBy) {
      case 'price-asc':
        return [...list].sort((a, b) => Number(a.price) - Number(b.price))
      case 'price-desc':
        return [...list].sort((a, b) => Number(b.price) - Number(a.price))
      case 'title':
        return [...list].sort((a, b) => (a.title || '').localeCompare(b.title || ''))
      default:
        return list
    }
  }, [products, search, sortBy])

  return (
    <div>
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-gray-900">Products</h1>
        <p className="text-sm text-gray-500">
          {!loading && !error ? `${visibleProducts.length} available` : ''}
        </p>
      </div>
      <p className="mt-1 text-sm text-gray-500">
        Browse approved digital products from verified sellers.
      </p>

      <div className="mt-4 flex flex-col gap-3 sm:flex-row sm:items-center">
        <input
          type="text"
          value={search}
          onChange={(event) => setSearch(event.target.value)}
          placeholder="Search products..."
          className="w-full rounded-md border border-gray-300 bg-white px-3 py-2 text-sm text-gray-900 focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500 sm:max-w-xs"
        />
        <select
          value={categoryId}
          onChange={(event) => setCategoryId(event.target.value)}
          className="w-full rounded-md border border-gray-300 bg-white px-3 py-2 text-sm text-gray-900 focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500 sm:w-auto"
        >
          <option value="">All categories</option>
          {categories.map((category) => (
            <option key={category.id} value={category.id}>
              {category.name}
            </option>
          ))}
        </select>
        <select
          value={sortBy}
          onChange={(event) => setSortBy(event.target.value)}
          className="w-full rounded-md border border-gray-300 bg-white px-3 py-2 text-sm text-gray-900 focus:border-indigo-500 focus:outline-none focus:ring-1 focus:ring-indigo-500 sm:w-auto"
        >
          <option value="default">Sort: Default</option>
          <option value="price-asc">Price: Low to High</option>
          <option value="price-desc">Price: High to Low</option>
          <option value="title">Title: A to Z</option>
        </select>
      </div>

      <div className="mt-6">
        {loading ? (
          <LoadingSpinner label="Loading products..." />
        ) : error ? (
          <ErrorState message={error} onRetry={() => setReload((count) => count + 1)} />
        ) : visibleProducts.length === 0 ? (
          <EmptyState
            title={products.length === 0 ? 'No products available' : 'No products match your search'}
            message={
              products.length === 0
                ? 'There are no approved products to display yet. Please check back later.'
                : 'Try adjusting your search, category, or sort options.'
            }
          />
        ) : (
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {visibleProducts.map((product) => (
              <ProductCard key={product.id} product={product} />
            ))}
          </div>
        )}
      </div>
    </div>
  )
}