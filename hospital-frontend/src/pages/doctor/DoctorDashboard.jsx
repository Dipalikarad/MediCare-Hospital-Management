// src/pages/doctor/DoctorDashboard.jsx
import { useEffect, useState } from 'react'
import { toast } from 'react-toastify'
import { getDoctorDashboard, getDoctorAppointments } from '../../api/doctorApi'
import { format } from 'date-fns'

const STATUS_COLORS = {
  PENDING: 'warning', CONFIRMED: 'primary', COMPLETED: 'success',
  CANCELLED: 'danger', NO_SHOW: 'secondary'
}

export default function DoctorDashboard() {
  const [stats, setStats]  = useState({})
  const [today, setToday]  = useState([])
  const [loading, setLoad] = useState(true)

  useEffect(() => {
    Promise.all([
      getDoctorDashboard(),
      getDoctorAppointments({ page: 0, size: 5 })
    ])
      .then(([s, a]) => {
        setStats(s.data.data || {})
        setToday(a.data.data?.content || [])
      })
      .catch(() => toast.error('Failed to load dashboard'))
      .finally(() => setLoad(false))
  }, [])

  if (loading) return <div className="spinner-border text-primary"></div>

  const cards = [
    { label: "Today's Appointments", value: stats.todayAppointments, color: 'primary',   icon: 'bi-calendar-today' },
    { label: 'Pending',              value: stats.pendingCount,       color: 'warning',   icon: 'bi-clock' },
    { label: 'Completed',            value: stats.completedCount,     color: 'success',   icon: 'bi-check-circle' }
  ]

  return (
    <div>
      <h4 className="fw-bold mb-4"><i className="bi bi-speedometer2 me-2 text-success"></i>Doctor Dashboard</h4>

      <div className="row g-3 mb-4">
        {cards.map(c => (
          <div key={c.label} className="col-md-4">
            <div className={`card stat-card border-start border-4 border-${c.color}`}>
              <div className="card-body d-flex justify-content-between align-items-center">
                <div>
                  <div className="text-muted small fw-semibold">{c.label}</div>
                  <div className="fs-2 fw-bold">{c.value ?? '—'}</div>
                </div>
                <i className={`bi ${c.icon} fs-1 text-${c.color} opacity-25`}></i>
              </div>
            </div>
          </div>
        ))}
      </div>

      <div className="card shadow-sm">
        <div className="card-header fw-semibold bg-transparent">Recent Appointments</div>
        <div className="table-responsive">
          <table className="table table-hover align-middle mb-0">
            <thead className="table-light">
              <tr><th>Patient</th><th>Date</th><th>Time</th><th>Status</th><th>Reason</th></tr>
            </thead>
            <tbody>
              {today.length === 0
                ? <tr><td colSpan="5" className="text-center text-muted py-4">No recent appointments</td></tr>
                : today.map(a => (
                  <tr key={a.id}>
                    <td className="fw-semibold">{a.patientName}</td>
                    <td>{format(new Date(a.appointmentDate), 'dd MMM yyyy')}</td>
                    <td>{a.startTime}</td>
                    <td><span className={`badge bg-${STATUS_COLORS[a.status]}`}>{a.status}</span></td>
                    <td><small>{a.reason || '—'}</small></td>
                  </tr>
                ))
              }
            </tbody>
          </table>
        </div>
      </div>
    </div>
  )
}
