// src/routes/ProtectedRoute.jsx
// Wraps routes that require authentication and/or specific roles

import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

/**
 * @param {string[]} allowedRoles - e.g. ['ADMIN'] or ['DOCTOR','ADMIN']
 */
export default function ProtectedRoute({ allowedRoles = [] }) {
  const { user, loading, isAuthenticated } = useAuth()
  const location = useLocation()

  if (loading) {
    return (
      <div className="spinner-overlay">
        <div className="spinner-border text-primary" role="status">
          <span className="visually-hidden">Loading...</span>
        </div>
      </div>
    )
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />
  }

  if (allowedRoles.length > 0 && !allowedRoles.includes(user.role)) {
    // Redirect to the user's own dashboard
    const dashMap = { ADMIN: '/admin/dashboard', DOCTOR: '/doctor/dashboard', PATIENT: '/patient/dashboard' }
    return <Navigate to={dashMap[user.role] || '/'} replace />
  }

  return <Outlet />
}
