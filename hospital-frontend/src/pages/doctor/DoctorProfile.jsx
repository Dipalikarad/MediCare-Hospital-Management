// src/pages/doctor/DoctorProfile.jsx
import { useCallback, useEffect, useState } from 'react'
import { useForm } from 'react-hook-form'
import { toast } from 'react-toastify'
import { getDoctorProfile, updateDoctorProfile } from '../../api/doctorApi'
import { getDepartments } from '../../api/patientApi'

export default function DoctorProfile() {
  const [profile, setProfile]   = useState(null)
  const [depts, setDepts]       = useState([])
  const [loading, setLoad]      = useState(true)
  const [editing, setEditing]   = useState(false)

  const { register, handleSubmit, reset, formState: { isSubmitting } } = useForm()

  const load = useCallback(async () => {
    setLoad(true)
    try {
      const [pRes, dRes] = await Promise.all([getDoctorProfile(), getDepartments()])
      setProfile(pRes.data.data)
      setDepts(dRes.data.data?.content || [])
      reset(pRes.data.data)
    } catch { toast.error('Failed to load profile') }
    setLoad(false)
  }, [reset])
  useEffect(() => { load() }, [load])

  const onSubmit = async (data) => {
    try {
      await updateDoctorProfile({ ...data, departmentId: Number(data.departmentId) })
      toast.success('Profile updated')
      setEditing(false); load()
    } catch (err) { toast.error(err?.response?.data?.message || 'Failed') }
  }

  if (loading) return <div className="spinner-border text-primary"></div>

  return (
    <div>
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h4 className="fw-bold mb-0"><i className="bi bi-person-circle me-2 text-success"></i>My Profile</h4>
        {!editing && (
          <button className="btn btn-outline-success" onClick={() => setEditing(true)}>
            <i className="bi bi-pencil me-1"></i>Edit
          </button>
        )}
      </div>

      <div className="card shadow-sm">
        <div className="card-body">
          {!editing ? (
            <div className="row g-3">
              {[
                ['Full Name',       profile?.fullName],
                ['Email',           profile?.email],
                ['Employee ID',     profile?.employeeId],
                ['Phone',           profile?.phone],
                ['Qualification',   profile?.qualification],
                ['Experience',      profile?.experienceYears ? `${profile.experienceYears} years` : '—'],
                ['Consultation Fee',profile?.consultationFee ? `₹${profile.consultationFee}` : '—'],
                ['Department',      profile?.departmentName],
                ['Account Status',  profile?.accountStatus]
              ].map(([label, value]) => (
                <div key={label} className="col-md-4">
                  <div className="p-3 bg-light rounded">
                    <div className="text-muted small">{label}</div>
                    <div className="fw-semibold">{value || '—'}</div>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <form onSubmit={handleSubmit(onSubmit)}>
              <div className="row g-3">
                {[
                  { n:'fullName',       l:'Full Name',          type:'text' },
                  { n:'phone',          l:'Phone',              type:'text' },
                  { n:'qualification',  l:'Qualification',      type:'text' },
                  { n:'experienceYears',l:'Experience (years)', type:'number' },
                  { n:'consultationFee',l:'Fee (₹)',            type:'number' }
                ].map(({ n, l, type }) => (
                  <div className="col-md-4" key={n}>
                    <label className="form-label small fw-semibold">{l}</label>
                    <input type={type} className="form-control" {...register(n)} />
                  </div>
                ))}
                <div className="col-md-4">
                  <label className="form-label small fw-semibold">Department</label>
                  <select className="form-select" {...register('departmentId')}>
                    {depts.map(d => <option key={d.id} value={d.id}>{d.name}</option>)}
                  </select>
                </div>
              </div>
              <div className="mt-4 d-flex gap-2">
                <button type="submit" className="btn btn-success" disabled={isSubmitting}>
                  {isSubmitting && <span className="spinner-border spinner-border-sm me-2"></span>}
                  Save Changes
                </button>
                <button type="button" className="btn btn-outline-secondary" onClick={() => { setEditing(false); reset(profile) }}>
                  Cancel
                </button>
              </div>
            </form>
          )}
        </div>
      </div>
    </div>
  )
}
