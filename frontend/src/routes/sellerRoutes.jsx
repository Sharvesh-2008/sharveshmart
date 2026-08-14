import { Route } from 'react-router-dom'
import RoleRoute from '../components/auth/RoleRoute'
import SellerDashboard from '../pages/SellerDashboard'
import SellerProductForm from '../pages/SellerProductForm'

const SELLER_ROLES = ['SELLER']

export default function SellerRoutes() {
  return (
    <>
      <Route
        path="/seller/products"
        element={
          <RoleRoute roles={SELLER_ROLES}>
            <SellerDashboard />
          </RoleRoute>
        }
      />
      <Route
        path="/seller/products/new"
        element={
          <RoleRoute roles={SELLER_ROLES}>
            <SellerProductForm />
          </RoleRoute>
        }
      />
      <Route
        path="/seller/products/:id/edit"
        element={
          <RoleRoute roles={SELLER_ROLES}>
            <SellerProductForm />
          </RoleRoute>
        }
      />
    </>
  )
}
