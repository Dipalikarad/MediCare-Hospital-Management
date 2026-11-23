// src/pages/patient/PatientPrescriptions.jsx
import { useEffect, useState } from 'react'
import { toast } from 'react-toastify'
import { getMyPrescriptions } from '../../api/patientApi'
import { format } from 'date-fns'

export default function PatientPrescriptions() {
  const [prescriptions, setRx] = useState([])
  const [loading, setLoad]     = useState(true)

  useEffect(() => {
    getMyPrescriptions({ page: 0, size: 20 })
      .then(res => setRx(res.data.data?.content || []))
      .catch(() => toast.error('Failed to load prescriptions'))
      .finally(() => setLoad(false))
  }, [])

  if (loading) return <div className="text-center py-5"><div className="spinner-border text-primary"></div></div>

  return (
    <div>
      <h4 className="fw-bold mb-4">
        <i className="bi bi-capsule me-2 text-warning"></i>My Prescriptions
      </h4>

      {prescriptions.length === 0 ? (
        <div className="text-center text-muted py-5">
          <i className="bi bi-capsule fs-1 opacity-25"></i>
          <p className="mt-2">No prescriptions yet</p>
        </div>
      ) : (
        <div className="row g-4">
          {prescriptions.map(rx => (
            <div key={rx.id} className="col-12">
              <div className="card shadow-sm">
                <div className="card-header bg-transparent d-flex justify-content-between align-items-center">
                  <div>
                    <i className="bi bi-person-badge me-2 text-success"></i>
                    <strong>{rx.doctorName}</strong>
                  </div>
                  <small className="text-muted">
                    <i className="bi bi-calendar me-1"></i>
                    {format(new Date(rx.createdAt), 'dd MMM yyyy, HH:mm')}
                  </small>
                </div>
                <div className="card-body">
                  {rx.diagnosis && (
                    <div className="alert alert-info py-2 mb-3">
                      <strong>Diagnosis:</strong> {rx.diagnosis}
                    </div>
                  )}
                  {rx.advice && (
                    <p className="mb-3">
                      <i className="bi bi-info-circle me-2 text-primary"></i>
                      <strong>Advice:</strong> {rx.advice}
                    </p>
                  )}

                  <h6 className="fw-semibold mb-2">Medicines</h6>
                  <div className="table-responsive">
                    <table className="table table-sm table-bordered mb-0">
                      <thead className="table-light">
                        <tr>
                          <th>Medicine</th><th>Dosage</th><th>Frequency</th>
                          <th>Duration</th><th>Instructions</th>
                        </tr>
                      </thead>
                      <tbody>
                        {rx.items?.map((item, i) => (
                          <tr key={i}>
                            <td className="fw-semibold">{item.medicineName}</td>
                            <td>{item.dosage || '—'}</td>
                            <td>{item.frequency || '—'}</td>
                            <td>{item.durationDays ? `${item.durationDays} days` : '—'}</td>
                            <td><small>{item.instructions || '—'}</small></td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
