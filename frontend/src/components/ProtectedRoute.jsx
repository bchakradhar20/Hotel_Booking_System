import { Navigate } from 'react-router-dom'

/**
 * Wraps routes that require an authenticated user.
 * Redirects to /login if no JWT token is found in localStorage.
 */
export default function ProtectedRoute({ children }) {
  const token = localStorage.getItem('token')
  return token ? children : <Navigate to="/login" replace />
}
