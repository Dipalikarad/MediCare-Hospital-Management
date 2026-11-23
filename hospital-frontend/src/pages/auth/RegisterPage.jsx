// src/pages/auth/RegisterPage.jsx
import { useForm } from 'react-hook-form'
import { yupResolver } from '@hookform/resolvers/yup'
import * as yup from 'yup'
import { useNavigate, Link } from 'react-router-dom'
import { toast } from 'react-toastify'
import { registerApi } from '../../api/authApi'
import { useAuth } from '../../context/AuthContext'

const schema = yup.object({
  fullName: yup.string().required('Full name is required').max(150),
  email:    yup.string().email('Invalid email').required('Email is required'),
  password: yup.string().min(8, 'At least 8 characters').required('Password is required'),
  confirm:  yup.string().oneOf([yup.ref('password')], 'Passwords must match').required()
})

export default function RegisterPage() {
  const { login } = useAuth()
  const navigate  = useNavigate()

  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm({
    resolver: yupResolver(schema)
  })

  const onSubmit = async ({ fullName, email, password }) => {
    try {
      const res = await registerApi({ fullName, email, password })
      login(res.data.data)
      toast.success('Account created! Welcome to MediCare.')
      navigate('/patient/dashboard')
    } catch (err) {
      toast.error(err?.response?.data?.message || 'Registration failed')
    }
  }

  return (
    <div className="auth-container">
      <div className="auth-card card p-4 p-md-5">
        <div className="text-center mb-4">
          <i className="bi bi-hospital fs-1 text-primary"></i>
          <h4 className="fw-bold mt-2">Create Account</h4>
          <p className="text-muted small">Join MediCare as a Patient</p>
        </div>

        <form onSubmit={handleSubmit(onSubmit)} noValidate>
          {[
            { name: 'fullName', label: 'Full Name',       type: 'text',     icon: 'bi-person',      ph: 'John Doe' },
            { name: 'email',    label: 'Email Address',   type: 'email',    icon: 'bi-envelope',    ph: 'you@example.com' },
            { name: 'password', label: 'Password',        type: 'password', icon: 'bi-lock',        ph: 'Min 8 characters' },
            { name: 'confirm',  label: 'Confirm Password',type: 'password', icon: 'bi-lock-fill',   ph: 'Repeat password' }
          ].map(({ name, label, type, icon, ph }) => (
            <div className="mb-3" key={name}>
              <label className="form-label fw-semibold">{label}</label>
              <div className="input-group">
                <span className="input-group-text"><i className={`bi ${icon}`}></i></span>
                <input type={type}
                  className={`form-control ${errors[name] ? 'is-invalid' : ''}`}
                  placeholder={ph} {...register(name)} />
                {errors[name] && <div className="invalid-feedback">{errors[name].message}</div>}
              </div>
            </div>
          ))}

          <button type="submit" className="btn btn-primary w-100 mt-2" disabled={isSubmitting}>
            {isSubmitting && <span className="spinner-border spinner-border-sm me-2"></span>}
            {isSubmitting ? 'Creating account...' : 'Create Account'}
          </button>
        </form>

        <p className="text-center small text-muted mt-3">
          Already have an account? <Link to="/login" className="text-primary fw-semibold">Sign in</Link>
        </p>
      </div>
    </div>
  )
}
