// src/pages/patient/BookAppointment.jsx
import { useCallback, useEffect, useState } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { toast } from 'react-toastify'
import { getDoctorSlots, bookAppointment } from '../../api/patientApi'
import { format, addDays } from 'date-fns'

export default function BookAppointment() {
  const { state }    = useLocation()
  const navigate     = useNavigate()
  const doctor       = state?.doctor

  const [date, setDate]     = useState(format(addDays(new Date(), 1), 'yyyy-MM-dd'))
  const [slots, setSlots]   = useState([])
  const [loading, setLoad]  = useState(false)
  const [selected, setSelected] = useState(null)

  const { register, handleSubmit, formState: { isSubmitting } } = useForm()

  const loadSlots = useCallback(async () => {
    if (!doctor) return
    setLoad(true); setSelected(null)
    try {
      const res = await getDoctorSlots(doctor.id, date)
      setSlots(res.data.data || [])
    } catch { toast.error('Failed to load slots') }
    setLoad(false)
  }, [date, doctor])

  useEffect(() => {
    if (!doctor) { navigate('/patient/doctors'); return }
    loadSlots()
  }, [doctor, loadSlots, navigate])

  const onSubmit = async ({ reason }) => {
    if (!selected) { toast.warning('Please select a time slot'); return }
    try {
      await bookAppointment({
        doctorId: doctor.id,
        appointmentDate: date,
        startTime: selected,
        reason
      })
      toast.success('Appointment booked successfully!')
      navigate('/patient/appointments')
    } catch (err) { toast.error(err?.response?.data?.message || 'Booking failed') }
  }

  if (!doctor) return null

  return (
    <div>
      <h4 className="fw-bold mb-4"><i className="bi bi-calendar-plus me-2 text-primary"></i>Book Appointment</h4>

      {/* Doctor card */}
      <div className="card shadow-sm mb-4 p-3">
        <div className="d-flex gap-3 align-items-center">
          <div className="rounded-circle bg-primary bg-opacity-10 d-flex align-items-center justify-content-center"
            style={{ width: 60, height: 60 }}>
            <i className="bi bi-person-badge fs-2 text-primary"></i>
          </div>
          <div>
            <h5 className="fw-bold mb-0">{doctor.fullName}</h5>
            <span className="badge bg-info">{doctor.departmentName}</span>
            <p className="mb-0 mt-1 small">{doctor.qualification}</p>
            <strong className="text-success">₹{doctor.consultationFee}</strong>
          </div>
        </div>
      </div>

      <form onSubmit={handleSubmit(onSubmit)}>
        {/* Date picker */}
        <div className="card shadow-sm p-4 mb-4">
          <label className="form-label fw-semibold">Select Date</label>
          <input type="date" className="form-control w-auto"
            value={date} min={format(addDays(new Date(), 1), 'yyyy-MM-dd')}
            onChange={e => setDate(e.target.value)} />
        </div>

        {/* Slot grid */}
        <div className="card shadow-sm p-4 mb-4">
          <h6 className="fw-semibold mb-3">Available Slots for {format(new Date(date + 'T00:00'), 'dd MMM yyyy')}</h6>
          {loading
            ? <div className="spinner-border text-primary"></div>
            : slots.length === 0
              ? <p className="text-muted">No slots available on this date</p>
              : (
                <div className="d-flex flex-wrap gap-2">
                  {slots.map((slot, i) => (
                    <button key={i} type="button"
                      disabled={!slot.available}
                      onClick={() => setSelected(slot.startTime)}
                      className={`btn btn-sm ${
                        !slot.available ? 'btn-secondary disabled' :
                        selected === slot.startTime ? 'btn-primary' : 'btn-outline-primary'
                      }`}>
                      {slot.startTime}
                    </button>
                  ))}
                </div>
              )
          }
        </div>

        {/* Reason */}
        <div className="card shadow-sm p-4 mb-4">
          <label className="form-label fw-semibold">Reason for Visit (optional)</label>
          <textarea className="form-control" rows="3" placeholder="Describe your symptoms..."
            {...register('reason')}></textarea>
        </div>

        <button type="submit" className="btn btn-primary px-5" disabled={isSubmitting || !selected}>
          {isSubmitting && <span className="spinner-border spinner-border-sm me-2"></span>}
          <i className="bi bi-check-circle me-2"></i>Confirm Booking
        </button>
      </form>
    </div>
  )
}
