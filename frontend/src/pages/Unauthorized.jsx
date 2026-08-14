import { Link } from 'react-router-dom'

export default function Unauthorized() {
  return (
    <div className="flex flex-col items-center justify-center py-16 text-center">
      <p className="text-6xl font-bold text-indigo-600">401</p>
      <h1 className="mt-4 text-2xl font-bold text-gray-900">Unauthorized</h1>
      <p className="mt-2 max-w-md text-sm text-gray-500">
        You need to be logged in to access this content. Your session may have expired.
      </p>
      <div className="mt-6 flex gap-3">
        <Link
          to="/login"
          className="rounded-md bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700"
        >
          Log in
        </Link>
        <Link
          to="/register"
          className="rounded-md border border-gray-300 px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50"
        >
          Register
        </Link>
      </div>
    </div>
  )
}