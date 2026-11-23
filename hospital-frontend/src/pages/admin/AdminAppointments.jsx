// src/pages/admin/AdminAppointments.jsx
import { useCallback, useEffect, useState } from 'react'
import { toast } from 'react-toastify'
import { getAllAppointments } from '../../api/adminApi'
import { format } from 'date-fns'

const STATUS_COLORS = {
  PENDING: 'warning', CONFIRMED: 'primary', COMPLETED: 'success',
  CANCELLED: 'danger', NO_SHOW: 'secondary'
}

export default function AdminAppointments() {
  const [appointments, setAppts] = useState([])
  const [loading, setLoading]    = useState(true)
  const [filters, setFilters]    = useState({ status: '', from: '', to: '', page: 0 })

  const load = useCallback(async () => {
    setLoading(true)
    try {
      const params = { page: filters.page, size: 15 }
      if (filters.status) params.status = filters.status
      if (filters.from)   params.from   = filters.from
      if (filters.to)     params.to     = filters.to
      const res = await getAllAppointments(params)
      setAppts(res.data.data?.content || [])
    } catch { toast.error('Failed to load') }
    setLoading(false)
  }, [filters])

  useEffect(() => { load() }, [load])

  return (
    <div>
      <h4 className="fw-bold mb-4"><i className="bi bi-calendar-check me-2 text-primary"></i>All Appointments</h4>

      {/* Filters */}
      <div className="card shadow-sm mb-4 p-3">
        <div className="row g-3">
          <div className="col-md-3">
            <select className="form-select" value={filters.status}
              onChange={e => setFilters(f => ({ ...f, status: e.target.value, page: 0 }))}>
              <option value="">All Statuses</option>
              {['PENDING','CONFIRMED','COMPLETED','CANCELLED','NO_SHOW'].map(s =>
                <option key={s} value={s}>{s}</option>)}
            </select>
          </div>
          <div className="col-md-3">
            <input type="date" className="form-control" placeholder="From"
              value={filters.from}
              onChange={e => setFilters(f => ({ ...f, from: e.target.value, page: 0 }))} />
          </div>
          <div className="col-md-3">
            <input type="date" className="form-control" placeholder="To"
              value={filters.to}
              onChange={e => setFilters(f => ({ ...f, to: e.target.value, page: 0 }))} />
          </div>
          <div className="col-md-3">
            <button className="btn btn-outline-secondary" onClick={() => setFilters({ status: '', from: '', to: '', page: 0 })}>
              Reset Filters
            </button>
          </div>
        </div>
      </div>

      {loading ? <div className="spinner-border text-primary"></div> : (
        <div className="card shadow-sm">
          <div className="table-responsive">
            <table className="table table-hover align-middle mb-0">
              <thead className="table-light">
                <tr><th>ID</th><th>Patient</th><th>Doctor</th><th>Department</th><th>Date</th><th>Time</th><th>Status</th><th>Reason</th></tr>
              </thead>
              <tbody>
                {appointments.length === 0
                  ? <tr><td colSpan="8" className="text-center text-muted py-4">No appointments</td></tr>
                  : appointments.map(a => (
                    <tr key={a.id}>
                      <td><code>#{a.id}</code></td>
                      <td>{a.patientName}</td>
                      <td>{a.doctorName}</td>
                      <td>{a.departmentName}</td>
                      <td>{format(new Date(a.appointmentDate), 'dd MMM yyyy')}</td>
                      <td>{a.startTime} – {a.endTime}</td>
                      <td><span className={`badge bg-${STATUS_COLORS[a.status]}`}>{a.status}</span></td>
                      <td><small className="text-muted">{a.reason || '—'}</small></td>
                    </tr>
                  ))
                }
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  )
}
