// src/layouts/DoctorLayout.jsx
import { Outlet, NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

const NAV_ITEMS = [
  { to: '/doctor/dashboard',     icon: 'bi-speedometer2',   label: 'Dashboard' },
  { to: '/doctor/appointments',  icon: 'bi-calendar-check', label: 'Appointments' },
  { to: '/doctor/availability',  icon: 'bi-clock',          label: 'Availability' },
  { to: '/doctor/prescriptions', icon: 'bi-capsule',        label: 'Prescriptions' },
  { to: '/doctor/profile',       icon: 'bi-person-circle',  label: 'My Profile' },
]

export default function DoctorLayout() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  return (
    <div className="d-flex">
      <nav className="sidebar d-flex flex-column p-3">
        <div className="text-center mb-4 mt-2">
          <i className="bi bi-hospital fs-2 text-success"></i>
          <div className="text-white fw-bold mt-1">Doctor Portal</div>
          <small className="text-muted">{user?.email}</small>
        </div>
        <ul className="nav flex-column flex-grow-1">
          {NAV_ITEMS.map(({ to, icon, label }) => (
            <li key={to} className="nav-item">
              <NavLink to={to} className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
                <i className={`bi ${icon}`}></i>{label}
              </NavLink>
            </li>
          ))}
        </ul>
        <button onClick={() => { logout(); navigate('/login') }}
          className="btn btn-outline-danger btn-sm mt-3">
          <i className="bi bi-box-arrow-right me-2"></i>Logout
        </button>
      </nav>
      <div className="main-content flex-grow-1">
        <div className="top-navbar">
          <h6 className="mb-0 fw-semibold">Doctor Portal</h6>
          <div className="d-flex align-items-center gap-2">
            <i className="bi bi-person-circle fs-5 text-success"></i>
            <span className="small">{user?.email}</span>
          </div>
        </div>
        <div className="p-4"><Outlet /></div>
      </div>
    </div>
  )
}
