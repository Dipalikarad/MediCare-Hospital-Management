// src/pages/patient/PatientDoctors.jsx
import { useCallback, useEffect, useState } from 'react'
import { toast } from 'react-toastify'
import { searchDoctors, getDepartments } from '../../api/patientApi'
import { useNavigate } from 'react-router-dom'

export default function PatientDoctors() {
  const [doctors, setDoctors]   = useState([])
  const [depts, setDepts]       = useState([])
  const [loading, setLoad]      = useState(true)
  const [search, setSearch]     = useState('')
  const [deptFilter, setDept]   = useState('')
  const navigate = useNavigate()

  const load = useCallback(async () => {
    setLoad(true)
    try {
      const params = { page: 0, size: 20 }
      if (search) params.name = search
      if (deptFilter) params.departmentId = deptFilter
      const [dRes, depRes] = await Promise.all([
        searchDoctors(params), getDepartments()
      ])
      setDoctors(dRes.data.data?.content || [])
      setDepts(depRes.data.data?.content || [])
    } catch { toast.error('Failed to load doctors') }
    setLoad(false)
  }, [deptFilter, search])
  useEffect(() => { load() }, [load])

  return (
    <div>
      <h4 className="fw-bold mb-4"><i className="bi bi-person-badge me-2 text-info"></i>Find a Doctor</h4>

      <div className="row g-3 mb-4">
        <div className="col-md-6">
          <input className="form-control" placeholder="Search by doctor name..."
            value={search} onChange={e => setSearch(e.target.value)} />
        </div>
        <div className="col-md-4">
          <select className="form-select" value={deptFilter} onChange={e => setDept(e.target.value)}>
            <option value="">All Departments</option>
            {depts.map(d => <option key={d.id} value={d.id}>{d.name}</option>)}
          </select>
        </div>
      </div>

      {loading ? <div className="spinner-border text-primary"></div> : (
        <div className="row g-4">
          {doctors.length === 0
            ? <p className="text-muted text-center py-5">No doctors found</p>
            : doctors.map(d => (
              <div key={d.id} className="col-md-4 col-lg-3">
                <div className="card h-100 shadow-sm text-center">
                  <div className="card-body">
                    <div className="rounded-circle bg-primary bg-opacity-10 d-inline-flex align-items-center
                                    justify-content-center mb-3" style={{ width: 64, height: 64 }}>
                      <i className="bi bi-person-badge fs-2 text-primary"></i>
                    </div>
                    <h6 className="fw-bold">{d.fullName}</h6>
                    <span className="badge bg-info mb-2">{d.departmentName}</span>
                    <p className="small text-muted mb-1">{d.qualification || '—'}</p>
                    <p className="small mb-1"><i className="bi bi-clock me-1"></i>{d.experienceYears} yrs exp</p>
                    <p className="fw-semibold text-success mb-3">₹{d.consultationFee}</p>
                    <button className="btn btn-primary btn-sm w-100"
                      onClick={() => navigate('/patient/book', { state: { doctor: d } })}>
                      <i className="bi bi-calendar-plus me-1"></i>Book Appointment
                    </button>
                  </div>
                </div>
              </div>
            ))
          }
        </div>
      )}
    </div>
  )
}
