// src/pages/patient/PatientHistory.jsx
import { useEffect, useState } from 'react'
import { toast } from 'react-toastify'
import { getMyHistory } from '../../api/patientApi'
import { format } from 'date-fns'

export default function PatientHistory() {
  const [records, setRecords] = useState([])
  const [loading, setLoad]    = useState(true)

  useEffect(() => {
    getMyHistory({ page: 0, size: 20 })
      .then(res => setRecords(res.data.data?.content || []))
      .catch(() => toast.error('Failed to load medical history'))
      .finally(() => setLoad(false))
  }, [])

  if (loading) return <div className="text-center py-5"><div className="spinner-border text-primary"></div></div>

  return (
    <div>
      <h4 className="fw-bold mb-4">
        <i className="bi bi-journal-medical me-2 text-primary"></i>Medical History
      </h4>

      {records.length === 0 ? (
        <div className="text-center text-muted py-5">
          <i className="bi bi-journal-medical fs-1 opacity-25"></i>
          <p className="mt-2">No medical records found</p>
        </div>
      ) : (
        <div className="timeline">
          {records.map(r => (
            <div key={r.id} className="card shadow-sm mb-3">
              <div className="card-body">
                <div className="d-flex justify-content-between align-items-start mb-2">
                  <div>
                    <h6 className="fw-bold mb-0">
                      <i className="bi bi-file-medical me-2 text-primary"></i>
                      {r.diagnosis || 'Medical Record'}
                    </h6>
                    <small className="text-muted">
                      <i className="bi bi-person-badge me-1"></i>
                      Dr. {r.doctorName}
                    </small>
                  </div>
                  <span className="badge bg-light text-dark">
                    <i className="bi bi-calendar me-1"></i>
                    {format(new Date(r.recordDate), 'dd MMM yyyy')}
                  </span>
                </div>

                {r.symptoms && (
                  <div className="mb-2">
                    <strong className="small text-muted">Symptoms:</strong>
                    <p className="mb-0 small">{r.symptoms}</p>
                  </div>
                )}

                {r.treatmentNotes && (
                  <div>
                    <strong className="small text-muted">Treatment Notes:</strong>
                    <p className="mb-0 small">{r.treatmentNotes}</p>
                  </div>
                )}

                {r.appointmentId && (
                  <div className="mt-2">
                    <small className="text-muted">
                      <i className="bi bi-link-45deg me-1"></i>
                      Linked to Appointment #{r.appointmentId}
                    </small>
                  </div>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
