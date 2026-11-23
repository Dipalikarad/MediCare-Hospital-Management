// src/pages/doctor/DoctorAvailability.jsx
import { useEffect, useState } from 'react'
import { useForm } from 'react-hook-form'
import { toast } from 'react-toastify'
import { getAvailability, addAvailability, deleteAvailability } from '../../api/doctorApi'

const DAYS = ['','Monday','Tuesday','Wednesday','Thursday','Friday','Saturday','Sunday']

export default function DoctorAvailability() {
  const [slots, setSlots]  = useState([])
  const [loading, setLoad] = useState(true)
  const [showForm, setShow]= useState(false)

  const { register, handleSubmit, reset, formState: { errors, isSubmitting } } = useForm({
    defaultValues: { slotDurationMinutes: 30, active: true }
  })

  const load = async () => {
    setLoad(true)
    try {
      const res = await getAvailability()
      setSlots(res.data.data || [])
    } catch { toast.error('Failed') }
    setLoad(false)
  }
  useEffect(() => { load() }, [])

  const onSubmit = async (data) => {
    try {
      await addAvailability({
        ...data,
        dayOfWeek: Number(data.dayOfWeek),
        slotDurationMinutes: Number(data.slotDurationMinutes)
      })
      toast.success('Availability added')
      reset(); setShow(false); load()
    } catch (err) { toast.error(err?.response?.data?.message || 'Failed') }
  }

  const handleDelete = async (id) => {
    if (!confirm('Remove this availability?')) return
    try { await deleteAvailability(id); toast.success('Removed'); load() }
    catch (err) { toast.error(err?.response?.data?.message || 'Failed') }
  }

  return (
    <div>
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h4 className="fw-bold mb-0"><i className="bi bi-clock me-2 text-success"></i>Availability Schedule</h4>
        <button className="btn btn-success" onClick={() => setShow(s => !s)}>
          <i className="bi bi-plus-lg me-1"></i>{showForm ? 'Cancel' : 'Add Slot'}
        </button>
      </div>

      {showForm && (
        <div className="card shadow-sm mb-4 p-4">
          <h6 className="fw-semibold mb-3">Add Availability</h6>
          <form onSubmit={handleSubmit(onSubmit)}>
            <div className="row g-3">
              <div className="col-md-3">
                <label className="form-label small fw-semibold">Day of Week</label>
                <select className={`form-select ${errors.dayOfWeek ? 'is-invalid' : ''}`}
                  {...register('dayOfWeek', { required: true })}>
                  <option value="">Select day</option>
                  {DAYS.slice(1).map((d, i) => <option key={i+1} value={i+1}>{d}</option>)}
                </select>
              </div>
              <div className="col-md-2">
                <label className="form-label small fw-semibold">Start Time</label>
                <input type="time" className="form-control" {...register('startTime', { required: true })} />
              </div>
              <div className="col-md-2">
                <label className="form-label small fw-semibold">End Time</label>
                <input type="time" className="form-control" {...register('endTime', { required: true })} />
              </div>
              <div className="col-md-2">
                <label className="form-label small fw-semibold">Slot (min)</label>
                <select className="form-select" {...register('slotDurationMinutes')}>
                  {[10,15,20,30,45,60].map(m => <option key={m} value={m}>{m} min</option>)}
                </select>
              </div>
              <div className="col-md-2 d-flex align-items-end">
                <button type="submit" className="btn btn-success w-100" disabled={isSubmitting}>
                  {isSubmitting && <span className="spinner-border spinner-border-sm me-2"></span>}
                  Save
                </button>
              </div>
            </div>
          </form>
        </div>
      )}

      {loading ? <div className="spinner-border text-primary"></div> : (
        slots.length === 0
          ? <div className="text-center text-muted py-5"><i className="bi bi-clock fs-1 opacity-25"></i><p className="mt-2">No availability set yet</p></div>
          : (
            <div className="row g-3">
              {slots.map(s => (
                <div key={s.id} className="col-md-4">
                  <div className={`card h-100 border-start border-4 border-${s.active ? 'success' : 'secondary'}`}>
                    <div className="card-body">
                      <h6 className="fw-bold">{DAYS[s.dayOfWeek]}</h6>
                      <p className="mb-1"><i className="bi bi-clock me-2 text-muted"></i>{s.startTime} – {s.endTime}</p>
                      <p className="mb-1"><i className="bi bi-stopwatch me-2 text-muted"></i>{s.slotDurationMinutes} min slots</p>
                      <span className={`badge bg-${s.active ? 'success' : 'secondary'}`}>{s.active ? 'Active' : 'Inactive'}</span>
                    </div>
                    <div className="card-footer">
                      <button className="btn btn-sm btn-outline-danger" onClick={() => handleDelete(s.id)}>
                        <i className="bi bi-trash me-1"></i>Remove
                      </button>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )
      )}
    </div>
  )
}
