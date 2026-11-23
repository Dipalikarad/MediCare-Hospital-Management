// src/layouts/AdminLayout.jsx
import { Outlet, NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

const NAV_ITEMS = [
  { to: '/admin/dashboard',    icon: 'bi-speedometer2',     label: 'Dashboard' },
  { to: '/admin/doctors',      icon: 'bi-person-badge',     label: 'Doctors' },
  { to: '/admin/patients',     icon: 'bi-people',           label: 'Patients' },
  { to: '/admin/departments',  icon: 'bi-building',         label: 'Departments' },
  { to: '/admin/appointments', icon: 'bi-calendar-check',   label: 'Appointments' },
  { to: '/admin/reports',      icon: 'bi-bar-chart-line',   label: 'Reports' },
]

export default function AdminLayout() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  const handleLogout = () => { logout(); navigate('/login') }

  return (
    <div className="d-flex">
      {/* ── Sidebar ── */}
      <nav className="sidebar d-flex flex-column p-3">
        <div className="text-center mb-4 mt-2">
          <i className="bi bi-hospital fs-2 text-primary"></i>
          <div className="text-white fw-bold mt-1">MediCare Admin</div>
          <small className="text-muted">{user?.email}</small>
        </div>
        <ul className="nav flex-column flex-grow-1">
          {NAV_ITEMS.map(({ to, icon, label }) => (
            <li key={to} className="nav-item">
              <NavLink to={to} className={({ isActive }) =>
                `nav-link ${isActive ? 'active' : ''}`
              }>
                <i className={`bi ${icon}`}></i>
                {label}
              </NavLink>
            </li>
          ))}
        </ul>
        <button onClick={handleLogout} className="btn btn-outline-danger btn-sm mt-3">
          <i className="bi bi-box-arrow-right me-2"></i>Logout
        </button>
      </nav>

      {/* ── Main area ── */}
      <div className="main-content flex-grow-1">
        <div className="top-navbar">
          <h6 className="mb-0 fw-semibold">Administration Panel</h6>
          <div className="d-flex align-items-center gap-2">
            <i className="bi bi-person-circle fs-5"></i>
            <span className="small">{user?.email}</span>
          </div>
        </div>
        <div className="p-4">
          <Outlet />
        </div>
      </div>
    </div>
  )
}
