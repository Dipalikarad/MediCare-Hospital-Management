// src/pages/admin/AdminDashboard.jsx
import { useEffect, useState } from 'react'
import { toast } from 'react-toastify'
import { getDashboard } from '../../api/adminApi'

const StatCard = ({ title, value, icon, color }) => (
  <div className="col-md-3 col-sm-6 mb-4">
    <div className={`card stat-card border-start border-4 border-${color}`}>
      <div className="card-body d-flex justify-content-between align-items-center">
        <div>
          <div className="text-muted small fw-semibold text-uppercase">{title}</div>
          <div className="fs-2 fw-bold mt-1">{value ?? '—'}</div>
        </div>
        <i className={`bi ${icon} fs-1 text-${color} opacity-25`}></i>
      </div>
    </div>
  </div>
)

export default function AdminDashboard() {
  const [stats, setStats] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    getDashboard()
      .then(res => setStats(res.data.data))
      .catch(() => toast.error('Failed to load dashboard'))
      .finally(() => setLoading(false))
  }, [])

  if (loading) return <div className="text-center py-5"><div className="spinner-border text-primary"></div></div>

  return (
    <div>
      <h4 className="fw-bold mb-4"><i className="bi bi-speedometer2 me-2 text-primary"></i>Admin Dashboard</h4>
      <div className="row">
        <StatCard title="Total Doctors"       value={stats?.totalDoctors}         icon="bi-person-badge"    color="primary" />
        <StatCard title="Total Patients"      value={stats?.totalPatients}        icon="bi-people"          color="info" />
        <StatCard title="Today's Appointments"value={stats?.todayAppointments}    icon="bi-calendar-today"  color="success" />
        <StatCard title="Pending"             value={stats?.pendingAppointments}  icon="bi-clock"           color="warning" />
        <StatCard title="Completed"           value={stats?.completedAppointments}icon="bi-check-circle"    color="success" />
        <StatCard title="Cancelled"           value={stats?.cancelledAppointments}icon="bi-x-circle"        color="danger" />
        <StatCard title="Total Appointments"  value={stats?.totalAppointments}    icon="bi-calendar-check"  color="secondary" />
        <StatCard title="Departments"         value={stats?.totalDepartments}     icon="bi-building"        color="dark" />
      </div>

      <div className="alert alert-info mt-2">
        <i className="bi bi-info-circle me-2"></i>
        Use the sidebar to manage Doctors, Patients, Departments, Appointments and view Reports.
      </div>
    </div>
  )
}
