import { Route } from 'react-router-dom'
import RoleRoute from '../components/auth/RoleRoute'
import Orders from '../pages/Orders'
import OrderDetails from '../pages/OrderDetails'
import Library from '../pages/Library'

export default function LibraryRoutes() {
  return (
    <>
      <Route
        path="/orders"
        element={
          <RoleRoute roles={['USER']}>
            <Orders />
          </RoleRoute>
        }
      />
      <Route
        path="/orders/:orderId"
        element={
          <RoleRoute roles={['USER']}>
            <OrderDetails />
          </RoleRoute>
        }
      />
      <Route
        path="/library"
        element={
          <RoleRoute roles={['USER']}>
            <Library />
          </RoleRoute>
        }
      />
    </>
  )
}
