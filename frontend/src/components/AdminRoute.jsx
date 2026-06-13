import { Navigate } from 'react-router-dom'

/**
 * Wraps routes that require ROLE_ADMIN.
 * Redirects to /rooms if the user is not an admin or not logged in.
 */
export default function AdminRoute({ children }) {
  const token = localStorage.getItem('token')
  const roles = JSON.parse(localStorage.getItem('roles') || '[]')
  if (!token) return <Navigate to="/login" replace />
  if (!roles.includes('ROLE_ADMIN')) return <Navigate to="/rooms" replace />
  return children
}
