// src/pages/HomePage.jsx
import { Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function HomePage() {
  const { isAuthenticated, user } = useAuth()

  const dashMap = {
    ADMIN: '/admin/dashboard',
    DOCTOR: '/doctor/dashboard',
    PATIENT: '/patient/dashboard'
  }

  return (
    <div className="min-vh-100" style={{
      background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
    }}>
      {/* Top Navbar */}
      <nav className="navbar navbar-dark px-4 py-3">
        <span className="navbar-brand fw-bold">
          <i className="bi bi-hospital me-2"></i>MediCare
        </span>
        <div className="d-flex gap-3">
          {isAuthenticated ? (
            <Link to={dashMap[user?.role] || '/'} className="btn btn-light btn-sm">
              <i className="bi bi-speedometer2 me-1"></i>Dashboard
            </Link>
          ) : (
            <>
              <Link to="/login" className="btn btn-outline-light btn-sm">Sign In</Link>
              <Link to="/register" className="btn btn-light btn-sm">Register</Link>
            </>
          )}
        </div>
      </nav>

      {/* Hero */}
      <div className="container text-center text-white py-5">
        <div className="py-5">
          <h1 className="display-3 fw-bold mb-3">
            Hospital Appointment &<br />Patient Management System
          </h1>
          <p className="lead opacity-75 mb-4" style={{ maxWidth: 600, margin: '0 auto' }}>
            Book appointments with top specialists, manage your medical history,
            and access prescriptions — all in one place.
          </p>
          {!isAuthenticated && (
            <div className="d-flex gap-3 justify-content-center">
              <Link to="/register" className="btn btn-light btn-lg px-4">
                <i className="bi bi-person-plus me-2"></i>Get Started
              </Link>
              <Link to="/login" className="btn btn-outline-light btn-lg px-4">
                <i className="bi bi-box-arrow-in-right me-2"></i>Sign In
              </Link>
            </div>
          )}
        </div>
      </div>

      {/* Features */}
      <div className="container pb-5">
        <div className="row g-4">
          {[
            {
              icon: 'bi-calendar-check',
              title: 'Easy Booking',
              desc: 'Browse doctors by specialization and book appointments in just a few clicks.'
            },
            {
              icon: 'bi-capsule',
              title: 'Digital Prescriptions',
              desc: 'Access your prescriptions and medical records anytime, anywhere.'
            },
            {
              icon: 'bi-shield-check',
              title: 'Secure & Private',
              desc: 'Your health data is protected with enterprise-grade JWT security.'
            }
          ].map(({ icon, title, desc }) => (
            <div key={title} className="col-md-4">
              <div className="card bg-white bg-opacity-10 border-0 text-white text-center h-100">
                <div className="card-body p-4">
                  <i className={`bi ${icon} fs-1 mb-3 d-block`}></i>
                  <h5 className="fw-bold">{title}</h5>
                  <p className="opacity-75 small mb-0">{desc}</p>
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Footer */}
      <div className="text-center text-white py-4 opacity-50 small">
        © 2026 MediCare Hospital Management System. All rights reserved.
      </div>
    </div>
  )
}
