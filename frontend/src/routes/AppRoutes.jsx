import { Route, Routes } from 'react-router-dom'
import RootLayout from '../layouts/RootLayout'
import ProtectedRoute from '../components/auth/ProtectedRoute'
import RoleRoute from '../components/auth/RoleRoute'
import Home from '../pages/Home'
import Login from '../pages/Login'
import Register from '../pages/Register'
import Products from '../pages/Products'
import ProductDetails from '../pages/ProductDetails'
import NotFound from '../pages/NotFound'
import Unauthorized from '../pages/Unauthorized'
import Forbidden from '../pages/Forbidden'
import CartRoutes from './cartRoutes'
import LibraryRoutes from './libraryRoutes'
import SellerRoutes from './sellerRoutes'
import AdminRoutes from './adminRoutes'

const AUTHENTICATED_ROLES = ['USER', 'SELLER', 'ADMIN']

export default function AppRoutes() {
  return (
    <Routes>
      <Route element={<RootLayout />}>
        <Route path="/" element={<Home />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route
          path="/products"
          element={
            <RoleRoute roles={AUTHENTICATED_ROLES}>
              <Products />
            </RoleRoute>
          }
        />
        <Route
          path="/products/:productId"
          element={
            <ProtectedRoute>
              <ProductDetails />
            </ProtectedRoute>
          }
        />
        <Route path="/unauthorized" element={<Unauthorized />} />
        <Route path="/forbidden" element={<Forbidden />} />
        <CartRoutes />
        <LibraryRoutes />
        <SellerRoutes />
        <AdminRoutes />
        <Route path="*" element={<NotFound />} />
      </Route>
    </Routes>
  )
}