import { useState } from 'react'
import { Link, NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../../hooks/useAuth'

export default function Navbar() {
  const { isAuthenticated, user, logout } = useAuth()
  const navigate = useNavigate()
  const [open, setOpen] = useState(false)

  const handleLogout = () => {
    logout()
    setOpen(false)
    navigate('/')
  }

  const navLinkClasses = ({ isActive }) =>
    `block rounded-md px-3 py-2 text-sm font-medium ${
      isActive ? 'bg-indigo-100 text-indigo-700' : 'text-gray-700 hover:bg-gray-100'
    }`

  return (
    <header className="sticky top-0 z-20 border-b border-gray-200 bg-white">
      <nav className="mx-auto flex h-16 max-w-6xl items-center justify-between px-4 sm:px-6">
        <div className="flex items-center gap-6">
          <Link to="/" className="flex items-center gap-2">
            <span className="flex h-8 w-8 items-center justify-center rounded-md bg-indigo-600 text-sm font-bold text-white">
              SM
            </span>
            <span className="text-sm font-semibold text-gray-900 sm:text-base">
              Sharvesh Mart
            </span>
          </Link>
          <div className="hidden items-center gap-1 md:flex">
            <NavLink to="/" end className={navLinkClasses}>
              Home
            </NavLink>
            <NavLink to="/products" className={navLinkClasses}>
              Products
            </NavLink>
            {user?.role === 'USER' ? (
              <>
                <NavLink to="/cart" className={navLinkClasses}>
                  Cart
                </NavLink>
                <NavLink to="/orders" className={navLinkClasses}>
                  Orders
                </NavLink>
                <NavLink to="/library" className={navLinkClasses}>
                  Library
                </NavLink>
              </>
            ) : null}
            {user?.role === 'SELLER' ? (
              <NavLink to="/seller/products" className={navLinkClasses}>
                My Products
              </NavLink>
            ) : null}
            {user?.role === 'ADMIN' ? (
              <NavLink to="/admin/moderation" className={navLinkClasses}>
                Moderation
              </NavLink>
            ) : null}
          </div>
        </div>

        <div className="hidden items-center gap-3 md:flex">
          {isAuthenticated ? (
            <>
              <span className="text-sm text-gray-600">
                {user?.name}
                <span className="ml-2 rounded-full bg-gray-100 px-2 py-0.5 text-xs font-medium text-gray-600">
                  {user?.role}
                </span>
              </span>
              <button
                type="button"
                onClick={handleLogout}
                className="rounded-md border border-gray-300 px-3 py-1.5 text-sm font-medium text-gray-700 hover:bg-gray-50"
              >
                Logout
              </button>
            </>
          ) : (
            <>
              <NavLink to="/login" className={navLinkClasses}>
                Login
              </NavLink>
              <NavLink
                to="/register"
                className="rounded-md bg-indigo-600 px-3 py-1.5 text-sm font-medium text-white hover:bg-indigo-700"
              >
                Register
              </NavLink>
            </>
          )}
        </div>

        <button
          type="button"
          className="rounded-md p-2 text-gray-700 hover:bg-gray-100 md:hidden"
          onClick={() => setOpen((current) => !current)}
          aria-label="Toggle navigation"
        >
          <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="2">
            {open ? (
              <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
            ) : (
              <path strokeLinecap="round" strokeLinejoin="round" d="M4 6h16M4 12h16M4 18h16" />
            )}
          </svg>
        </button>
      </nav>

      {open ? (
        <div className="border-t border-gray-200 bg-white px-4 pb-4 pt-2 md:hidden">
          <div className="flex flex-col gap-1">
            <NavLink to="/" end className={navLinkClasses} onClick={() => setOpen(false)}>
              Home
            </NavLink>
            <NavLink to="/products" className={navLinkClasses} onClick={() => setOpen(false)}>
              Products
            </NavLink>
            {user?.role === 'USER' ? (
              <>
                <NavLink to="/cart" className={navLinkClasses} onClick={() => setOpen(false)}>
                  Cart
                </NavLink>
                <NavLink to="/orders" className={navLinkClasses} onClick={() => setOpen(false)}>
                  Orders
                </NavLink>
                <NavLink to="/library" className={navLinkClasses} onClick={() => setOpen(false)}>
                  Library
                </NavLink>
              </>
            ) : null}
            {user?.role === 'SELLER' ? (
              <NavLink to="/seller/products" className={navLinkClasses} onClick={() => setOpen(false)}>
                My Products
              </NavLink>
            ) : null}
            {user?.role === 'ADMIN' ? (
              <NavLink to="/admin/moderation" className={navLinkClasses} onClick={() => setOpen(false)}>
                Moderation
              </NavLink>
            ) : null}
            {isAuthenticated ? (
              <>
                <p className="px-3 py-2 text-sm text-gray-600">
                  Signed in as {user?.name} ({user?.role})
                </p>
                <button
                  type="button"
                  onClick={handleLogout}
                  className="rounded-md border border-gray-300 px-3 py-2 text-left text-sm font-medium text-gray-700 hover:bg-gray-50"
                >
                  Logout
                </button>
              </>
            ) : (
              <>
                <NavLink to="/login" className={navLinkClasses} onClick={() => setOpen(false)}>
                  Login
                </NavLink>
                <NavLink to="/register" className={navLinkClasses} onClick={() => setOpen(false)}>
                  Register
                </NavLink>
              </>
            )}
          </div>
        </div>
      ) : null}
    </header>
  )
}