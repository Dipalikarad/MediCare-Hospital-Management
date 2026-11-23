// src/pages/patient/PatientAppointments.jsx
import { useCallback, useEffect, useState } from 'react'
import { toast } from 'react-toastify'
import { getMyAppointments, cancelAppointment } from '../../api/patientApi'
import { format } from 'date-fns'

const STATUS_COLORS = {
  PENDING: 'warning', CONFIRMED: 'primary', COMPLETED: 'success',
  CANCELLED: 'danger', NO_SHOW: 'secondary'
}

export default function PatientAppointments() {
  const [appointments, setAppts] = useState([])
  const [loading, setLoading]    = useState(true)
  const [statusFilter, setFilter] = useState('')
  const [page, setPage]           = useState(0)
  const [totalPages, setTotal]    = useState(0)
  const [cancelId, setCancelId]   = useState(null)
  const [cancelReason, setCancelReason] = useState('')

  const load = useCallback(async () => {
    setLoading(true)
    try {
      const params = { page, size: 10 }
      if (statusFilter) params.status = statusFilter
      const res = await getMyAppointments(params)
      setAppts(res.data.data?.content || [])
      setTotal(res.data.data?.totalPages || 0)
    } catch { toast.error('Failed to load appointments') }
    setLoading(false)
  }, [page, statusFilter])

  useEffect(() => { load() }, [load])

  const handleCancel = async () => {
    if (!cancelId) return
    try {
      await cancelAppointment(cancelId, { cancelReason })
      toast.success('Appointment cancelled')
      setCancelId(null); setCancelReason(''); load()
    } catch (err) {
      toast.error(err?.response?.data?.message || 'Failed to cancel')
    }
  }

  return (
    <div>
      <h4 className="fw-bold mb-4">
        <i className="bi bi-calendar-check me-2 text-info"></i>My Appointments
      </h4>

      {/* Status filter */}
      <div className="mb-3">
        <select className="form-select w-auto"
          value={statusFilter} onChange={e => { setFilter(e.target.value); setPage(0) }}>
          <option value="">All Statuses</option>
          {['PENDING','CONFIRMED','COMPLETED','CANCELLED','NO_SHOW'].map(s =>
            <option key={s} value={s}>{s}</option>)}
        </select>
      </div>

      {loading ? <div className="spinner-border text-primary"></div> : (
        <div className="card shadow-sm">
          <div className="table-responsive">
            <table className="table table-hover align-middle mb-0">
              <thead className="table-light">
                <tr>
                  <th>#</th><th>Doctor</th><th>Department</th>
                  <th>Date</th><th>Time</th><th>Status</th><th>Reason</th><th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {appointments.length === 0
                  ? <tr><td colSpan="8" className="text-center text-muted py-4">No appointments</td></tr>
                  : appointments.map((a, i) => (
                    <tr key={a.id}>
                      <td>{page * 10 + i + 1}</td>
                      <td className="fw-semibold">{a.doctorName}</td>
                      <td>{a.departmentName}</td>
                      <td>{format(new Date(a.appointmentDate), 'dd MMM yyyy')}</td>
                      <td>{a.startTime} – {a.endTime}</td>
                      <td><span className={`badge bg-${STATUS_COLORS[a.status]}`}>{a.status}</span></td>
                      <td><small className="text-muted">{a.reason || '—'}</small></td>
                      <td>
                        {['PENDING','CONFIRMED'].includes(a.status) && (
                          <button className="btn btn-sm btn-outline-danger"
                            onClick={() => setCancelId(a.id)}>
                            <i className="bi bi-x-circle me-1"></i>Cancel
                          </button>
                        )}
                        {a.cancelReason && (
                          <small className="d-block text-danger mt-1">
                            Cancel: {a.cancelReason}
                          </small>
                        )}
                      </td>
                    </tr>
                  ))
                }
              </tbody>
            </table>
          </div>

          {/* Pagination */}
          {totalPages > 1 && (
            <div className="card-footer d-flex gap-2 justify-content-center">
              {Array.from({ length: totalPages }, (_, i) => (
                <button key={i}
                  className={`btn btn-sm ${page === i ? 'btn-primary' : 'btn-outline-primary'}`}
                  onClick={() => setPage(i)}>{i + 1}</button>
              ))}
            </div>
          )}
        </div>
      )}

      {/* Cancel Modal */}
      {cancelId && (
        <div className="modal d-block" style={{ background: 'rgba(0,0,0,0.5)' }}>
          <div className="modal-dialog">
            <div className="modal-content">
              <div className="modal-header">
                <h5 className="modal-title">Cancel Appointment</h5>
                <button className="btn-close" onClick={() => setCancelId(null)}></button>
              </div>
              <div className="modal-body">
                <p className="text-muted">Are you sure you want to cancel this appointment?</p>
                <label className="form-label fw-semibold">Reason (optional)</label>
                <textarea className="form-control" rows="3"
                  value={cancelReason}
                  onChange={e => setCancelReason(e.target.value)}
                  placeholder="e.g. Scheduling conflict..."></textarea>
              </div>
              <div className="modal-footer">
                <button className="btn btn-secondary" onClick={() => setCancelId(null)}>
                  Close
                </button>
                <button className="btn btn-danger" onClick={handleCancel}>
                  <i className="bi bi-x-circle me-1"></i>Confirm Cancel
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
