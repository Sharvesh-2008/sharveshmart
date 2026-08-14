import { Link } from 'react-router-dom'

export default function Forbidden() {
  return (
    <div className="flex flex-col items-center justify-center py-16 text-center">
      <p className="text-6xl font-bold text-indigo-600">403</p>
      <h1 className="mt-4 text-2xl font-bold text-gray-900">Forbidden</h1>
      <p className="mt-2 max-w-md text-sm text-gray-500">
        Your account does not have permission to access this resource.
      </p>
      <Link
        to="/products"
        className="mt-6 rounded-md bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700"
      >
        Back to products
      </Link>
    </div>
  )
}