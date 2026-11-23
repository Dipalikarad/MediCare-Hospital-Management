// src/pages/admin/AdminDepartments.jsx
import { useEffect, useState } from 'react'
import { useForm } from 'react-hook-form'
import { toast } from 'react-toastify'
import { getDepartments, createDepartment, updateDepartment, deleteDepartment } from '../../api/adminApi'

export default function AdminDepartments() {
  const [depts, setDepts]       = useState([])
  const [loading, setLoading]   = useState(true)
  const [editing, setEditing]   = useState(null)
  const [showModal, setShow]    = useState(false)

  const { register, handleSubmit, reset, setValue, formState: { errors, isSubmitting } } = useForm()

  const load = async () => {
    setLoading(true)
    try {
      const res = await getDepartments()
      setDepts(res.data.data?.content || [])
    } catch { toast.error('Failed to load') }
    setLoading(false)
  }
  useEffect(() => { load() }, [])

  const openCreate = () => { reset(); setEditing(null); setShow(true) }
  const openEdit   = (d) => { setValue('name', d.name); setValue('description', d.description); setEditing(d); setShow(true) }

  const onSubmit = async (data) => {
    try {
      if (editing) { await updateDepartment(editing.id, data); toast.success('Updated') }
      else         { await createDepartment(data);             toast.success('Created') }
      setShow(false); reset(); load()
    } catch (err) { toast.error(err?.response?.data?.message || 'Failed') }
  }

  const handleDelete = async (id) => {
    if (!confirm('Delete this department?')) return
    try { await deleteDepartment(id); toast.success('Deleted'); load() }
    catch (err) { toast.error(err?.response?.data?.message || 'Cannot delete — may have doctors') }
  }

  return (
    <div>
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h4 className="fw-bold mb-0"><i className="bi bi-building me-2 text-primary"></i>Departments</h4>
        <button className="btn btn-primary" onClick={openCreate}><i className="bi bi-plus-lg me-1"></i>Add</button>
      </div>

      {loading ? <div className="spinner-border text-primary"></div> : (
        <div className="row g-3">
          {depts.map(d => (
            <div key={d.id} className="col-md-4">
              <div className="card h-100 shadow-sm">
                <div className="card-body">
                  <h6 className="fw-bold"><i className="bi bi-building me-2 text-primary"></i>{d.name}</h6>
                  <p className="text-muted small mb-0">{d.description || 'No description'}</p>
                </div>
                <div className="card-footer d-flex gap-2">
                  <button className="btn btn-sm btn-outline-primary" onClick={() => openEdit(d)}>Edit</button>
                  <button className="btn btn-sm btn-outline-danger"  onClick={() => handleDelete(d.id)}>Delete</button>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      {showModal && (
        <div className="modal d-block" style={{ background: 'rgba(0,0,0,0.5)' }}>
          <div className="modal-dialog">
            <div className="modal-content">
              <div className="modal-header">
                <h5 className="modal-title">{editing ? 'Edit' : 'Add'} Department</h5>
                <button className="btn-close" onClick={() => setShow(false)}></button>
              </div>
              <form onSubmit={handleSubmit(onSubmit)}>
                <div className="modal-body">
                  <div className="mb-3">
                    <label className="form-label fw-semibold">Name</label>
                    <input className={`form-control ${errors.name ? 'is-invalid' : ''}`}
                      {...register('name', { required: 'Name is required' })} />
                    {errors.name && <div className="invalid-feedback">{errors.name.message}</div>}
                  </div>
                  <div className="mb-3">
                    <label className="form-label fw-semibold">Description</label>
                    <textarea className="form-control" rows="3" {...register('description')}></textarea>
                  </div>
                </div>
                <div className="modal-footer">
                  <button type="button" className="btn btn-secondary" onClick={() => setShow(false)}>Cancel</button>
                  <button type="submit" className="btn btn-primary" disabled={isSubmitting}>
                    {isSubmitting && <span className="spinner-border spinner-border-sm me-2"></span>}
                    {editing ? 'Update' : 'Create'}
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
