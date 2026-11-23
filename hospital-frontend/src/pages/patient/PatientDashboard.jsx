// src/pages/patient/PatientDashboard.jsx
import { useEffect, useState } from 'react'
import { toast } from 'react-toastify'
import { getMyAppointments, getMyPrescriptions } from '../../api/patientApi'
import { Link } from 'react-router-dom'
import { format } from 'date-fns'

const STATUS_COLORS = { PENDING:'warning', CONFIRMED:'primary', COMPLETED:'success', CANCELLED:'danger', NO_SHOW:'secondary' }

export default function PatientDashboard() {
  const [upcoming, setUpcoming]     = useState([])
  const [prescriptions, setRx]      = useState([])
  const [loading, setLoad]          = useState(true)

  useEffect(() => {
    Promise.all([
      getMyAppointments({ status: 'PENDING', page: 0, size: 5 }),
      getMyPrescriptions({ page: 0, size: 3 })
    ])
      .then(([aRes, rxRes]) => {
        setUpcoming(aRes.data.data?.content || [])
        setRx(rxRes.data.data?.content || [])
      })
      .catch(() => toast.error('Failed to load dashboard'))
      .finally(() => setLoad(false))
  }, [])

  if (loading) return <div className="spinner-border text-primary"></div>

  return (
    <div>
      <h4 className="fw-bold mb-4"><i className="bi bi-house me-2 text-info"></i>My Dashboard</h4>

      {/* Quick actions */}
      <div className="row g-3 mb-4">
        <div className="col-md-4">
          <Link to="/patient/doctors" className="card stat-card text-decoration-none border-start border-4 border-primary">
            <div className="card-body d-flex gap-3 align-items-center">
              <i className="bi bi-search fs-2 text-primary"></i>
              <div><div className="fw-semibold">Find a Doctor</div><div className="small text-muted">Browse by specialty</div></div>
            </div>
          </Link>
        </div>
        <div className="col-md-4">
          <Link to="/patient/appointments" className="card stat-card text-decoration-none border-start border-4 border-success">
            <div className="card-body d-flex gap-3 align-items-center">
              <i className="bi bi-calendar-check fs-2 text-success"></i>
              <div><div className="fw-semibold">My Appointments</div><div className="small text-muted">View all</div></div>
            </div>
          </Link>
        </div>
        <div className="col-md-4">
          <Link to="/patient/prescriptions" className="card stat-card text-decoration-none border-start border-4 border-warning">
            <div className="card-body d-flex gap-3 align-items-center">
              <i className="bi bi-capsule fs-2 text-warning"></i>
              <div><div className="fw-semibold">Prescriptions</div><div className="small text-muted">View all</div></div>
            </div>
          </Link>
        </div>
      </div>

      {/* Upcoming appointments */}
      <div className="card shadow-sm mb-4">
        <div className="card-header bg-transparent fw-semibold d-flex justify-content-between">
          <span>Upcoming Appointments</span>
          <Link to="/patient/appointments" className="btn btn-sm btn-outline-primary">View All</Link>
        </div>
        <div className="table-responsive">
          <table className="table table-hover align-middle mb-0">
            <thead className="table-light">
              <tr><th>Doctor</th><th>Department</th><th>Date</th><th>Time</th><th>Status</th></tr>
            </thead>
            <tbody>
              {upcoming.length === 0
                ? <tr><td colSpan="5" className="text-center text-muted py-3">No upcoming appointments</td></tr>
                : upcoming.map(a => (
                  <tr key={a.id}>
                    <td className="fw-semibold">{a.doctorName}</td>
                    <td>{a.departmentName}</td>
                    <td>{format(new Date(a.appointmentDate), 'dd MMM yyyy')}</td>
                    <td>{a.startTime}</td>
                    <td><span className={`badge bg-${STATUS_COLORS[a.status]}`}>{a.status}</span></td>
                  </tr>
                ))
              }
            </tbody>
          </table>
        </div>
      </div>

      {/* Recent prescriptions */}
      {prescriptions.length > 0 && (
        <div className="card shadow-sm">
          <div className="card-header bg-transparent fw-semibold d-flex justify-content-between">
            <span>Recent Prescriptions</span>
            <Link to="/patient/prescriptions" className="btn btn-sm btn-outline-primary">View All</Link>
          </div>
          <div className="card-body">
            {prescriptions.map(rx => (
              <div key={rx.id} className="border rounded p-3 mb-2">
                <div className="d-flex justify-content-between">
                  <strong><i className="bi bi-person-badge me-2"></i>{rx.doctorName}</strong>
                  <small className="text-muted">{format(new Date(rx.createdAt), 'dd MMM yyyy')}</small>
                </div>
                {rx.diagnosis && <p className="mb-1 mt-1 small">{rx.diagnosis}</p>}
                <small className="text-muted">{rx.items?.length} medicine(s)</small>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  )
}
