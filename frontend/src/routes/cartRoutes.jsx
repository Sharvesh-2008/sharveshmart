import { Route } from 'react-router-dom'
import RoleRoute from '../components/auth/RoleRoute'
import Cart from '../pages/Cart'
import Checkout from '../pages/Checkout'
import Payment from '../pages/Payment'

const BUYER_ROLES = ['USER']

export default function CartRoutes() {
  return (
    <>
      <Route
        path="/cart"
        element={
          <RoleRoute roles={BUYER_ROLES}>
            <Cart />
          </RoleRoute>
        }
      />
      <Route
        path="/checkout"
        element={
          <RoleRoute roles={BUYER_ROLES}>
            <Checkout />
          </RoleRoute>
        }
      />
      <Route
        path="/payment"
        element={
          <RoleRoute roles={BUYER_ROLES}>
            <Payment />
          </RoleRoute>
        }
      />
      <Route
        path="/payment/:orderId"
        element={
          <RoleRoute roles={BUYER_ROLES}>
            <Payment />
          </RoleRoute>
        }
      />
    </>
  )
}
