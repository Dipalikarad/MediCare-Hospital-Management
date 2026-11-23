// src/pages/patient/PatientProfilePage.jsx
import { useCallback, useEffect, useState } from 'react'
import { useForm } from 'react-hook-form'
import { yupResolver } from '@hookform/resolvers/yup'
import * as yup from 'yup'
import { toast } from 'react-toastify'
import { getPatientProfile, updatePatientProfile } from '../../api/patientApi'

const schema = yup.object({
  fullName:         yup.string().required('Full name is required').max(150),
  phone:            yup.string().nullable(),
  gender:           yup.string().nullable(),
  dateOfBirth:      yup.string().nullable(),
  bloodGroup:       yup.string().nullable(),
  address:          yup.string().nullable(),
  allergies:        yup.string().nullable(),
  emergencyContact: yup.string().nullable()
})

export default function PatientProfilePage() {
  const [profile, setProfile]   = useState(null)
  const [loading, setLoad]      = useState(true)
  const [editing, setEditing]   = useState(false)

  const { register, handleSubmit, reset, formState: { errors, isSubmitting } } = useForm({
    resolver: yupResolver(schema)
  })

  const load = useCallback(async () => {
    setLoad(true)
    try {
      const res = await getPatientProfile()
      const p = res.data.data
      setProfile(p)
      reset({
        ...p,
        dateOfBirth: p.dateOfBirth || ''
      })
    } catch { toast.error('Failed to load profile') }
    setLoad(false)
  }, [reset])

  useEffect(() => { load() }, [load])

  const onSubmit = async (data) => {
    try {
      // Clean up empty strings to null
      const payload = Object.fromEntries(
        Object.entries(data).map(([k, v]) => [k, v === '' ? null : v])
      )
      await updatePatientProfile(payload)
      toast.success('Profile updated successfully')
      setEditing(false)
      load()
    } catch (err) {
      toast.error(err?.response?.data?.message || 'Update failed')
    }
  }

  if (loading) return <div className="text-center py-5"><div className="spinner-border text-primary"></div></div>

  const fields = [
    { name: 'fullName',         label: 'Full Name',          type: 'text',   icon: 'bi-person' },
    { name: 'phone',            label: 'Phone',              type: 'text',   icon: 'bi-telephone' },
    { name: 'dateOfBirth',      label: 'Date of Birth',      type: 'date',   icon: 'bi-calendar' },
    { name: 'bloodGroup',       label: 'Blood Group',        type: 'text',   icon: 'bi-droplet' },
    { name: 'address',          label: 'Address',            type: 'text',   icon: 'bi-geo-alt' },
    { name: 'allergies',        label: 'Allergies',          type: 'text',   icon: 'bi-exclamation-triangle' },
    { name: 'emergencyContact', label: 'Emergency Contact',  type: 'text',   icon: 'bi-telephone-plus' }
  ]

  return (
    <div>
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h4 className="fw-bold mb-0">
          <i className="bi bi-person-circle me-2 text-info"></i>My Profile
        </h4>
        {!editing && (
          <button className="btn btn-outline-primary" onClick={() => setEditing(true)}>
            <i className="bi bi-pencil me-1"></i>Edit Profile
          </button>
        )}
      </div>

      <div className="card shadow-sm">
        <div className="card-body">
          {/* Email (read only) */}
          <div className="row mb-3">
            <div className="col-12">
              <div className="p-3 bg-light rounded">
                <div className="text-muted small">
                  <i className="bi bi-envelope me-1"></i>Email
                </div>
                <div className="fw-semibold">{profile?.email}</div>
              </div>
            </div>
          </div>

          {!editing ? (
            /* ── View Mode ── */
            <div className="row g-3">
              {fields.map(({ name, label, icon }) => (
                <div key={name} className="col-md-4">
                  <div className="p-3 bg-light rounded h-100">
                    <div className="text-muted small">
                      <i className={`bi ${icon} me-1`}></i>{label}
                    </div>
                    <div className="fw-semibold">{profile?.[name] || '—'}</div>
                  </div>
                </div>
              ))}
              {/* Gender (special) */}
              <div className="col-md-4">
                <div className="p-3 bg-light rounded h-100">
                  <div className="text-muted small">
                    <i className="bi bi-gender-ambiguous me-1"></i>Gender
                  </div>
                  <div className="fw-semibold">{profile?.gender || '—'}</div>
                </div>
              </div>
            </div>
          ) : (
            /* ── Edit Mode ── */
            <form onSubmit={handleSubmit(onSubmit)}>
              <div className="row g-3">
                {fields.map(({ name, label, type, icon }) => (
                  <div key={name} className="col-md-4">
                    <label className="form-label small fw-semibold">
                      <i className={`bi ${icon} me-1`}></i>{label}
                    </label>
                    <input type={type}
                      className={`form-control ${errors[name] ? 'is-invalid' : ''}`}
                      {...register(name)} />
                    {errors[name] && <div className="invalid-feedback">{errors[name].message}</div>}
                  </div>
                ))}
                {/* Gender dropdown */}
                <div className="col-md-4">
                  <label className="form-label small fw-semibold">
                    <i className="bi bi-gender-ambiguous me-1"></i>Gender
                  </label>
                  <select className="form-select" {...register('gender')}>
                    <option value="">Select...</option>
                    <option value="MALE">Male</option>
                    <option value="FEMALE">Female</option>
                    <option value="OTHER">Other</option>
                  </select>
                </div>
              </div>
              <div className="mt-4 d-flex gap-2">
                <button type="submit" className="btn btn-primary" disabled={isSubmitting}>
                  {isSubmitting && <span className="spinner-border spinner-border-sm me-2"></span>}
                  <i className="bi bi-check-circle me-1"></i>Save Changes
                </button>
                <button type="button" className="btn btn-outline-secondary"
                  onClick={() => { setEditing(false); reset(profile) }}>
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
