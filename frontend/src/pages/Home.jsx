import { Link } from 'react-router-dom'

const highlights = [
  'Browse a catalog of approved digital products',
  'Register as a buyer or a seller',
  'Secure JWT authentication with role-based access',
  'Cart, checkout, payment, digital library, and authorized downloads',
]

export default function Home() {
  return (
    <div>
      <section className="rounded-2xl bg-gradient-to-br from-indigo-600 to-indigo-800 px-6 py-16 text-center text-white sm:px-12">
        <h1 className="text-3xl font-bold tracking-tight sm:text-4xl">
          Sharvesh Mart
        </h1>
        <p className="mx-auto mt-4 max-w-2xl text-indigo-100">
          A place to buy and sell ebooks, software, templates, design assets, and courses — with
          verified purchase ownership and controlled, authorized downloads.
        </p>
        <div className="mt-6 flex justify-center gap-3">
          <Link
            to="/products"
            className="rounded-md bg-white px-5 py-2.5 text-sm font-semibold text-indigo-700 hover:bg-indigo-50"
          >
            Browse Products
          </Link>
          <Link
            to="/register"
            className="rounded-md border border-white/40 px-5 py-2.5 text-sm font-semibold text-white hover:bg-white/10"
          >
            Create an account
          </Link>
        </div>
      </section>

      <section className="mt-10 grid gap-4 sm:grid-cols-2">
        {highlights.map((item) => (
          <div key={item} className="rounded-lg border border-gray-200 bg-white p-5 shadow-sm">
            <div className="flex items-center gap-3">
              <span className="flex h-7 w-7 shrink-0 items-center justify-center rounded-full bg-indigo-100 text-sm font-bold text-indigo-700">
                ✓
              </span>
              <p className="text-sm text-gray-700">{item}</p>
            </div>
          </div>
        ))}
      </section>
    </div>
  )
}