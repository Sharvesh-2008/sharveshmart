import { Navigate } from 'react-router-dom'
import { useAuth } from '../../hooks/useAuth'
import ProtectedRoute from './ProtectedRoute'

export default function RoleRoute({ roles, children }) {
  const { user } = useAuth()

  return (
    <ProtectedRoute>
      {roles && user && !roles.includes(user.role) ? <Navigate to="/forbidden" replace /> : children}
    </ProtectedRoute>
  )
}