// src/pages/doctor/DoctorPrescriptions.jsx
import { useEffect, useState } from 'react'
import { useForm, useFieldArray } from 'react-hook-form'
import { toast } from 'react-toastify'
import { getDoctorPrescriptions, createPrescription, getDoctorAppointments } from '../../api/doctorApi'
import { format } from 'date-fns'

export default function DoctorPrescriptions() {
  const [prescriptions, setRx] = useState([])
  const [completedAppts, setAppts] = useState([])
  const [loading, setLoad]     = useState(true)
  const [showModal, setModal]  = useState(false)

  const { register, handleSubmit, control, reset, formState: { isSubmitting } } = useForm({
    defaultValues: { items: [{ medicineName: '', dosage: '', frequency: '', durationDays: 1, instructions: '' }] }
  })
  const { fields, append, remove } = useFieldArray({ control, name: 'items' })

  const load = async () => {
    setLoad(true)
    try {
      const [rxRes, apptRes] = await Promise.all([
        getDoctorPrescriptions({ page: 0, size: 20 }),
        getDoctorAppointments({ status: 'COMPLETED', page: 0, size: 100 })
      ])
      setRx(rxRes.data.data?.content || [])
      setAppts(apptRes.data.data?.content || [])
    } catch { toast.error('Failed') }
    setLoad(false)
  }
  useEffect(() => { load() }, [])

  const onSubmit = async (data) => {
    try {
      await createPrescription({ ...data, appointmentId: Number(data.appointmentId) })
      toast.success('Prescription created')
      reset(); setModal(false); load()
    } catch (err) { toast.error(err?.response?.data?.message || 'Failed') }
  }

  return (
    <div>
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h4 className="fw-bold mb-0"><i className="bi bi-capsule me-2 text-primary"></i>Prescriptions</h4>
        <button className="btn btn-primary" onClick={() => setModal(true)}>
          <i className="bi bi-plus-lg me-1"></i>Write Prescription
        </button>
      </div>

      {loading ? <div className="spinner-border text-primary"></div> : (
        <div className="row g-3">
          {prescriptions.length === 0
            ? <p className="text-muted text-center py-5">No prescriptions yet</p>
            : prescriptions.map(rx => (
              <div key={rx.id} className="col-12">
                <div className="card shadow-sm">
                  <div className="card-header bg-transparent d-flex justify-content-between">
                    <span><i className="bi bi-person me-2"></i><strong>{rx.patientName}</strong></span>
                    <small className="text-muted">{format(new Date(rx.createdAt), 'dd MMM yyyy, HH:mm')}</small>
                  </div>
                  <div className="card-body">
                    {rx.diagnosis && <p className="mb-2"><strong>Diagnosis:</strong> {rx.diagnosis}</p>}
                    {rx.advice    && <p className="mb-2"><strong>Advice:</strong> {rx.advice}</p>}
                    <div className="table-responsive">
                      <table className="table table-sm table-bordered mb-0">
                        <thead className="table-light">
                          <tr><th>Medicine</th><th>Dosage</th><th>Frequency</th><th>Duration</th><th>Instructions</th></tr>
                        </thead>
                        <tbody>
                          {rx.items?.map((item, i) => (
                            <tr key={i}>
                              <td>{item.medicineName}</td>
                              <td>{item.dosage}</td>
                              <td>{item.frequency}</td>
                              <td>{item.durationDays} days</td>
                              <td>{item.instructions || '—'}</td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    </div>
                  </div>
                </div>
              </div>
            ))
          }
        </div>
      )}

      {showModal && (
        <div className="modal d-block" style={{ background: 'rgba(0,0,0,0.5)', overflowY: 'auto' }}>
          <div className="modal-dialog modal-xl">
            <div className="modal-content">
              <div className="modal-header">
                <h5 className="modal-title">Write Prescription</h5>
                <button className="btn-close" onClick={() => { setModal(false); reset() }}></button>
              </div>
              <form onSubmit={handleSubmit(onSubmit)}>
                <div className="modal-body">
                  <div className="row g-3 mb-3">
                    <div className="col-md-6">
                      <label className="form-label small fw-semibold">Select Completed Appointment</label>
                      <select className="form-select" {...register('appointmentId', { required: true })}>
                        <option value="">Select appointment...</option>
                        {completedAppts.map(a => (
                          <option key={a.id} value={a.id}>
                            #{a.id} – {a.patientName} – {format(new Date(a.appointmentDate), 'dd MMM yyyy')}
                          </option>
                        ))}
                      </select>
                    </div>
                    <div className="col-md-6">
                      <label className="form-label small fw-semibold">Diagnosis</label>
                      <input className="form-control" {...register('diagnosis')} />
                    </div>
                    <div className="col-12">
                      <label className="form-label small fw-semibold">Advice / Notes</label>
                      <textarea className="form-control" rows="2" {...register('advice')}></textarea>
                    </div>
                  </div>

                  <h6 className="fw-semibold">Medicines</h6>
                  {fields.map((field, index) => (
                    <div key={field.id} className="row g-2 align-items-end mb-2 border-bottom pb-2">
                      {[
                        { n: 'medicineName', label: 'Medicine', type: 'text', required: true },
                        { n: 'dosage',       label: 'Dosage',   type: 'text' },
                        { n: 'frequency',    label: 'Frequency',type: 'text' },
                        { n: 'durationDays', label: 'Days',     type: 'number' },
                        { n: 'instructions', label: 'Notes',    type: 'text' }
                      ].map(({ n, label, type, required }) => (
                        <div className="col" key={n}>
                          <label className="form-label small">{label}</label>
                          <input type={type} className="form-control form-control-sm"
                            {...register(`items.${index}.${n}`, { required })} />
                        </div>
                      ))}
                      <div className="col-auto">
                        {fields.length > 1 &&
                          <button type="button" className="btn btn-sm btn-outline-danger"
                            onClick={() => remove(index)}><i className="bi bi-trash"></i></button>}
                      </div>
                    </div>
                  ))}
                  <button type="button" className="btn btn-sm btn-outline-primary mt-2"
                    onClick={() => append({ medicineName:'',dosage:'',frequency:'',durationDays:1,instructions:'' })}>
                    <i className="bi bi-plus-lg me-1"></i>Add Medicine
                  </button>
                </div>
                <div className="modal-footer">
                  <button type="button" className="btn btn-secondary" onClick={() => { setModal(false); reset() }}>Cancel</button>
                  <button type="submit" className="btn btn-primary" disabled={isSubmitting}>
                    {isSubmitting && <span className="spinner-border spinner-border-sm me-2"></span>}
                    Save Prescription
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
