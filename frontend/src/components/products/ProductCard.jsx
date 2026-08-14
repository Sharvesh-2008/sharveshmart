import { Link } from 'react-router-dom'
import { formatPrice } from '../../utils/format'

export default function ProductCard({ product }) {
  return (
    <Link
      to={`/products/${product.id}`}
      className="group flex flex-col rounded-lg border border-gray-200 bg-white p-5 shadow-sm transition hover:border-indigo-200 hover:shadow"
    >
      <div className="flex items-start justify-between gap-3">
        <h3 className="text-base font-semibold text-gray-900 group-hover:text-indigo-700">
          {product.title}
        </h3>
        <span className="whitespace-nowrap text-lg font-bold text-indigo-600">
          {formatPrice(product.price)}
        </span>
      </div>
      <div className="mt-3 flex flex-wrap gap-x-4 gap-y-1 text-sm text-gray-500">
        <span>Category: <span className="text-gray-700">{product.categoryName}</span></span>
        <span>Seller: <span className="text-gray-700">{product.sellerName}</span></span>
      </div>
    </Link>
  )
}