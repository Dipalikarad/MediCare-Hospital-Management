// src/pages/admin/AdminPatients.jsx
import { useEffect, useState } from 'react'
import { toast } from 'react-toastify'
import { getPatients, togglePatientStatus } from '../../api/adminApi'
import { format } from 'date-fns'

export default function AdminPatients() {
  const [patients, setPatients] = useState([])
  const [loading, setLoading]  = useState(true)
  const [page, setPage]        = useState(0)
  const [totalPages, setTotalPages] = useState(0)

  const load = async (p = 0) => {
    setLoading(true)
    try {
      const res = await getPatients({ page: p, size: 10 })
      const data = res.data.data
      setPatients(data.content || [])
      setTotalPages(data.totalPages || 0)
    } catch { toast.error('Failed to load patients') }
    setLoading(false)
  }

  useEffect(() => { load(page) }, [page])

  const toggleStatus = async (userId, status) => {
    if (!confirm(`${status === 'ACTIVE' ? 'Deactivate' : 'Activate'} this patient?`)) return
    try { await togglePatientStatus(userId); toast.success('Updated'); load(page) }
    catch { toast.error('Failed') }
  }

  return (
    <div>
      <h4 className="fw-bold mb-4"><i className="bi bi-people me-2 text-info"></i>Patients</h4>
      {loading
        ? <div className="spinner-border text-primary"></div>
        : (
          <div className="card shadow-sm">
            <div className="table-responsive">
              <table className="table table-hover align-middle mb-0">
                <thead className="table-light">
                  <tr><th>#</th><th>Name</th><th>Email</th><th>Phone</th><th>Blood Group</th><th>DOB</th><th>Status</th><th>Action</th></tr>
                </thead>
                <tbody>
                  {patients.length === 0
                    ? <tr><td colSpan="8" className="text-center text-muted py-4">No patients</td></tr>
                    : patients.map((p, i) => (
                      <tr key={p.id}>
                        <td>{page * 10 + i + 1}</td>
                        <td><div className="fw-semibold">{p.fullName}</div></td>
                        <td>{p.email}</td>
                        <td>{p.phone || '—'}</td>
                        <td>{p.bloodGroup || '—'}</td>
                        <td>{p.dateOfBirth ? format(new Date(p.dateOfBirth), 'dd MMM yyyy') : '—'}</td>
                        <td><span className={`badge bg-${p.accountStatus === 'ACTIVE' ? 'success' : 'danger'}`}>{p.accountStatus}</span></td>
                        <td>
                          <button
                            className={`btn btn-sm ${p.accountStatus === 'ACTIVE' ? 'btn-outline-danger' : 'btn-outline-success'}`}
                            onClick={() => toggleStatus(p.userId, p.accountStatus)}>
                            {p.accountStatus === 'ACTIVE' ? 'Deactivate' : 'Activate'}
                          </button>
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
        )
      }
    </div>
  )
}
