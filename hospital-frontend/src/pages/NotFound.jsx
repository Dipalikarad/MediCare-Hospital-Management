// src/pages/NotFound.jsx
import { Link } from 'react-router-dom'

export default function NotFound() {
  return (
    <div className="d-flex flex-column align-items-center justify-content-center min-vh-100 bg-light">
      <div className="text-center">
        <h1 className="display-1 fw-bold text-primary">404</h1>
        <h4 className="fw-semibold mb-3">Page Not Found</h4>
        <p className="text-muted mb-4">
          The page you&apos;re looking for doesn&apos;t exist or has been moved.
        </p>
        <Link to="/" className="btn btn-primary">
          <i className="bi bi-house me-2"></i>Go Home
        </Link>
      </div>
    </div>
  )
}
