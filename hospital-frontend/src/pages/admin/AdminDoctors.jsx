// src/pages/admin/AdminDoctors.jsx
import { useCallback, useEffect, useState } from 'react'
import { toast } from 'react-toastify'
import { useForm } from 'react-hook-form'
import { yupResolver } from '@hookform/resolvers/yup'
import * as yup from 'yup'
import { getDoctors, createDoctor, toggleDoctorStatus, getDepartments } from '../../api/adminApi'

const schema = yup.object({
  fullName: yup.string().required(), email: yup.string().email().required(),
  password: yup.string().min(8).required(), employeeId: yup.string().required(),
  phone: yup.string(), qualification: yup.string(),
  experienceYears: yup.number().min(0).max(60),
  consultationFee: yup.number().min(0),
  departmentId: yup.number().required('Department is required')
})

export default function AdminDoctors() {
  const [doctors, setDoctors]       = useState([])
  const [departments, setDepts]     = useState([])
  const [loading, setLoading]       = useState(true)
  const [showModal, setShowModal]   = useState(false)
  const [search, setSearch]         = useState('')

  const { register, handleSubmit, reset, formState: { errors, isSubmitting } } = useForm({
    resolver: yupResolver(schema)
  })

  const load = useCallback(async () => {
    setLoading(true)
    try {
      const [dRes, depRes] = await Promise.all([
        getDoctors({ name: search, page: 0, size: 50 }),
        getDepartments()
      ])
      setDoctors(dRes.data.data?.content || [])
      setDepts(depRes.data.data?.content || [])
    } catch { toast.error('Failed to load data') }
    setLoading(false)
  }, [search])

  useEffect(() => { load() }, [load])

  const onSubmit = async (data) => {
    try {
      await createDoctor({ ...data, departmentId: Number(data.departmentId) })
      toast.success('Doctor created successfully')
      reset(); setShowModal(false); load()
    } catch (err) { toast.error(err?.response?.data?.message || 'Failed') }
  }

  const toggleStatus = async (userId, currentStatus) => {
    if (!confirm(`${currentStatus === 'ACTIVE' ? 'Deactivate' : 'Activate'} this doctor?`)) return
    try {
      await toggleDoctorStatus(userId)
      toast.success('Status updated')
      load()
    } catch { toast.error('Failed') }
  }

  const statusBadge = (s) => (
    <span className={`badge badge-status bg-${s === 'ACTIVE' ? 'success' : 'danger'}`}>{s}</span>
  )

  return (
    <div>
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h4 className="fw-bold mb-0"><i className="bi bi-person-badge me-2 text-primary"></i>Doctors</h4>
        <button className="btn btn-primary" onClick={() => setShowModal(true)}>
          <i className="bi bi-plus-lg me-1"></i>Add Doctor
        </button>
      </div>

      <div className="mb-3">
        <input className="form-control w-50" placeholder="Search by name..."
          value={search} onChange={e => setSearch(e.target.value)} />
      </div>

      {loading ? <div className="spinner-border text-primary"></div> : (
        <div className="card shadow-sm">
          <div className="table-responsive">
            <table className="table table-hover align-middle mb-0">
              <thead className="table-light">
                <tr>
                  <th>#</th><th>Name</th><th>Employee ID</th><th>Department</th>
                  <th>Fee (₹)</th><th>Experience</th><th>Status</th><th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {doctors.length === 0
                  ? <tr><td colSpan="8" className="text-center text-muted py-4">No doctors found</td></tr>
                  : doctors.map((d, i) => (
                    <tr key={d.id}>
                      <td>{i + 1}</td>
                      <td><div className="fw-semibold">{d.fullName}</div><small className="text-muted">{d.email}</small></td>
                      <td><code>{d.employeeId}</code></td>
                      <td>{d.departmentName}</td>
                      <td>₹{d.consultationFee}</td>
                      <td>{d.experienceYears} yrs</td>
                      <td>{statusBadge(d.accountStatus)}</td>
                      <td>
                        <button
                          className={`btn btn-sm ${d.accountStatus === 'ACTIVE' ? 'btn-outline-danger' : 'btn-outline-success'}`}
                          onClick={() => toggleStatus(d.userId, d.accountStatus)}>
                          {d.accountStatus === 'ACTIVE' ? 'Deactivate' : 'Activate'}
                        </button>
                      </td>
                    </tr>
                  ))
                }
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Add Doctor Modal */}
      {showModal && (
        <div className="modal d-block" style={{ background: 'rgba(0,0,0,0.5)' }}>
          <div className="modal-dialog modal-lg">
            <div className="modal-content">
              <div className="modal-header">
                <h5 className="modal-title">Add New Doctor</h5>
                <button className="btn-close" onClick={() => { setShowModal(false); reset() }}></button>
              </div>
              <form onSubmit={handleSubmit(onSubmit)}>
                <div className="modal-body">
                  <div className="row g-3">
                    {[
                      { name: 'fullName',    label: 'Full Name',          type: 'text' },
                      { name: 'email',       label: 'Email',              type: 'email' },
                      { name: 'password',    label: 'Password',           type: 'password' },
                      { name: 'employeeId',  label: 'Employee ID',        type: 'text' },
                      { name: 'phone',       label: 'Phone',              type: 'text' },
                      { name: 'qualification',label:'Qualification',      type: 'text' },
                      { name: 'experienceYears',label:'Experience (yrs)', type: 'number' },
                      { name: 'consultationFee', label:'Consultation Fee (₹)',type:'number' }
                    ].map(({ name, label, type }) => (
                      <div className="col-md-6" key={name}>
                        <label className="form-label small fw-semibold">{label}</label>
                        <input type={type} className={`form-control ${errors[name] ? 'is-invalid' : ''}`}
                          {...register(name)} />
                        {errors[name] && <div className="invalid-feedback">{errors[name]?.message}</div>}
                      </div>
                    ))}
                    <div className="col-md-6">
                      <label className="form-label small fw-semibold">Department</label>
                      <select className={`form-select ${errors.departmentId ? 'is-invalid' : ''}`}
                        {...register('departmentId')}>
                        <option value="">Select department</option>
                        {departments.map(d => <option key={d.id} value={d.id}>{d.name}</option>)}
                      </select>
                      {errors.departmentId && <div className="invalid-feedback">{errors.departmentId.message}</div>}
                    </div>
                  </div>
                </div>
                <div className="modal-footer">
                  <button type="button" className="btn btn-secondary" onClick={() => { setShowModal(false); reset() }}>Cancel</button>
                  <button type="submit" className="btn btn-primary" disabled={isSubmitting}>
                    {isSubmitting && <span className="spinner-border spinner-border-sm me-2"></span>}
                    Create Doctor
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
