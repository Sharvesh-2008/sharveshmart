import { Route } from 'react-router-dom'
import RoleRoute from '../components/auth/RoleRoute'
import AdminModeration from '../pages/AdminModeration'

export default function AdminRoutes() {
  return (
    <>
      <Route
        path="/admin/moderation"
        element={
          <RoleRoute roles={['ADMIN']}>
            <AdminModeration />
          </RoleRoute>
        }
      />
    </>
  )
}
