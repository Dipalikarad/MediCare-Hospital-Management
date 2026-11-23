// src/pages/doctor/DoctorAppointments.jsx
import { useCallback, useEffect, useState } from 'react'
import { toast } from 'react-toastify'
import { getDoctorAppointments, updateAppointmentStatus } from '../../api/doctorApi'
import { format } from 'date-fns'

const STATUS_COLORS = { PENDING:'warning',CONFIRMED:'primary',COMPLETED:'success',CANCELLED:'danger',NO_SHOW:'secondary' }
const TRANSITIONS   = { PENDING:['CONFIRMED'], CONFIRMED:['COMPLETED','NO_SHOW','CANCELLED'] }

export default function DoctorAppointments() {
  const [appointments, setAppts] = useState([])
  const [loading, setLoading]    = useState(true)
  const [statusFilter, setFilter] = useState('')

  const load = useCallback(async () => {
    setLoading(true)
    try {
      const params = { page: 0, size: 50 }
      if (statusFilter) params.status = statusFilter
      const res = await getDoctorAppointments(params)
      setAppts(res.data.data?.content || [])
    } catch { toast.error('Failed to load') }
    setLoading(false)
  }, [statusFilter])
  useEffect(() => { load() }, [load])

  const changeStatus = async (id, status) => {
    if (!confirm(`Change status to ${status}?`)) return
    try {
      await updateAppointmentStatus(id, status)
      toast.success(`Appointment marked as ${status}`)
      load()
    } catch (err) { toast.error(err?.response?.data?.message || 'Failed') }
  }

  return (
    <div>
      <h4 className="fw-bold mb-4"><i className="bi bi-calendar-check me-2 text-primary"></i>My Appointments</h4>

      <div className="mb-3">
        <select className="form-select w-auto" value={statusFilter} onChange={e => setFilter(e.target.value)}>
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
                <tr><th>#</th><th>Patient</th><th>Date</th><th>Time</th><th>Status</th><th>Reason</th><th>Actions</th></tr>
              </thead>
              <tbody>
                {appointments.length === 0
                  ? <tr><td colSpan="7" className="text-center text-muted py-4">No appointments</td></tr>
                  : appointments.map((a, i) => (
                    <tr key={a.id}>
                      <td>{i + 1}</td>
                      <td className="fw-semibold">{a.patientName}</td>
                      <td>{format(new Date(a.appointmentDate), 'dd MMM yyyy')}</td>
                      <td>{a.startTime} – {a.endTime}</td>
                      <td><span className={`badge bg-${STATUS_COLORS[a.status]}`}>{a.status}</span></td>
                      <td><small>{a.reason || '—'}</small></td>
                      <td>
                        <div className="d-flex gap-1 flex-wrap">
                          {(TRANSITIONS[a.status] || []).map(next => (
                            <button key={next}
                              className={`btn btn-sm btn-outline-${STATUS_COLORS[next]}`}
                              onClick={() => changeStatus(a.id, next)}>
                              {next}
                            </button>
                          ))}
                        </div>
                      </td>
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
