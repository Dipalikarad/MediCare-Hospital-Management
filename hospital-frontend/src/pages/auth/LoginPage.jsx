// src/pages/auth/LoginPage.jsx
import { useForm } from 'react-hook-form'
import { yupResolver } from '@hookform/resolvers/yup'
import * as yup from 'yup'
import { useNavigate, Link } from 'react-router-dom'
import { toast } from 'react-toastify'
import { GoogleLogin } from '@react-oauth/google'

import { loginApi, googleLoginApi } from '../../api/authApi'
import { useAuth } from '../../context/AuthContext'

const schema = yup.object({
  email:    yup.string().email('Invalid email').required('Email is required'),
  password: yup.string().required('Password is required')
})

export default function LoginPage() {
  const { login } = useAuth()
  const navigate  = useNavigate()
  const googleConfigured = Boolean(import.meta.env.VITE_GOOGLE_CLIENT_ID?.trim())

  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm({
    resolver: yupResolver(schema)
  })

  // ── Email + Password login ────────────────────────────────
  const onSubmit = async (data) => {
    try {
      const res = await loginApi(data)
      login(res.data.data)
      toast.success('Welcome back!')
      redirectByRole(res.data.data.role)
    } catch (err) {
      toast.error(err?.response?.data?.message || 'Login failed')
    }
  }

  // ── Google login ──────────────────────────────────────────
  const handleGoogleSuccess = async (credentialResponse) => {
    try {
      const idToken = credentialResponse?.credential
      if (!idToken) throw new Error('Google did not return an ID token.')
      const res = await googleLoginApi(idToken)
      login(res.data.data)
      toast.success('Google login successful!')
      redirectByRole(res.data.data.role)
    } catch (err) {
      toast.error(err?.response?.data?.message || 'Google login failed')
    }
  }

  const redirectByRole = (role) => {
    const map = { ADMIN: '/admin/dashboard', DOCTOR: '/doctor/dashboard', PATIENT: '/patient/dashboard' }
    navigate(map[role] || '/')
  }

  return (
    <div className="auth-container">
      <div className="auth-card card p-4 p-md-5">
        {/* Logo */}
        <div className="text-center mb-4">
          <i className="bi bi-hospital fs-1 text-primary"></i>
          <h4 className="fw-bold mt-2">MediCare</h4>
          <p className="text-muted small">Hospital Management System</p>
        </div>

        <form onSubmit={handleSubmit(onSubmit)} noValidate>
          {/* Email */}
          <div className="mb-3">
            <label className="form-label fw-semibold">Email Address</label>
            <div className="input-group">
              <span className="input-group-text"><i className="bi bi-envelope"></i></span>
              <input type="email" className={`form-control ${errors.email ? 'is-invalid' : ''}`}
                placeholder="you@example.com" {...register('email')} />
              {errors.email && <div className="invalid-feedback">{errors.email.message}</div>}
            </div>
          </div>

          {/* Password */}
          <div className="mb-3">
            <label className="form-label fw-semibold">Password</label>
            <div className="input-group">
              <span className="input-group-text"><i className="bi bi-lock"></i></span>
              <input type="password" className={`form-control ${errors.password ? 'is-invalid' : ''}`}
                placeholder="Enter password" {...register('password')} />
              {errors.password && <div className="invalid-feedback">{errors.password.message}</div>}
            </div>
          </div>

          <div className="d-flex justify-content-end mb-3">
            <Link to="/forgot-password" className="small text-primary">Forgot password?</Link>
          </div>

          <button type="submit" className="btn btn-primary w-100 mb-3" disabled={isSubmitting}>
            {isSubmitting
              ? <span className="spinner-border spinner-border-sm me-2"></span>
              : <i className="bi bi-box-arrow-in-right me-2"></i>}
            {isSubmitting ? 'Signing in...' : 'Sign In'}
          </button>
        </form>

        {googleConfigured && <>
          <div className="d-flex align-items-center my-3">
            <hr className="flex-grow-1" />
            <span className="px-2 text-muted small">OR</span>
            <hr className="flex-grow-1" />
          </div>
          <div className="d-flex justify-content-center mb-3">
            <GoogleLogin
              onSuccess={handleGoogleSuccess}
              onError={() => toast.error('Google login failed. Check the OAuth web client configuration.')}
              useOneTap={false}
            />
          </div>
        </>}
        {!googleConfigured && <p className="small text-muted text-center mb-3">
          Google sign-in is not configured. Use email and password to sign in.
        </p>}
        <p className="text-center small text-muted mt-2">
          Don&apos;t have an account? <Link to="/register" className="text-primary fw-semibold">Register</Link>
        </p>
      </div>
    </div>
  )
}
