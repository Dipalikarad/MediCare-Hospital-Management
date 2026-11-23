// src/pages/auth/ForgotPasswordPage.jsx
import { useForm } from 'react-hook-form'
import { yupResolver } from '@hookform/resolvers/yup'
import * as yup from 'yup'
import { Link } from 'react-router-dom'
import { toast } from 'react-toastify'
import { forgotPasswordApi } from '../../api/authApi'

const schema = yup.object({ email: yup.string().email().required('Email is required') })

export default function ForgotPasswordPage() {
  const { register, handleSubmit, formState: { errors, isSubmitting, isSubmitSuccessful } } = useForm({
    resolver: yupResolver(schema)
  })

  const onSubmit = async (data) => {
    try {
      await forgotPasswordApi(data)
      toast.success('Reset link sent if that email exists.')
    } catch {
      toast.error('Something went wrong. Please try again.')
    }
  }

  return (
    <div className="auth-container">
      <div className="auth-card card p-4 p-md-5">
        <div className="text-center mb-4">
          <i className="bi bi-lock-fill fs-1 text-warning"></i>
          <h4 className="fw-bold mt-2">Forgot Password</h4>
          <p className="text-muted small">Enter your email to receive a reset link</p>
        </div>

        {isSubmitSuccessful ? (
          <div className="alert alert-success text-center">
            <i className="bi bi-check-circle me-2"></i>
            Check your email for the reset link.
            <div className="mt-2"><Link to="/login">Back to Login</Link></div>
          </div>
        ) : (
          <form onSubmit={handleSubmit(onSubmit)} noValidate>
            <div className="mb-3">
              <label className="form-label fw-semibold">Email Address</label>
              <div className="input-group">
                <span className="input-group-text"><i className="bi bi-envelope"></i></span>
                <input type="email"
                  className={`form-control ${errors.email ? 'is-invalid' : ''}`}
                  placeholder="you@example.com" {...register('email')} />
                {errors.email && <div className="invalid-feedback">{errors.email.message}</div>}
              </div>
            </div>
            <button type="submit" className="btn btn-primary w-100" disabled={isSubmitting}>
              {isSubmitting && <span className="spinner-border spinner-border-sm me-2"></span>}
              Send Reset Link
            </button>
            <p className="text-center mt-3 small">
              <Link to="/login">← Back to Login</Link>
            </p>
          </form>
        )}
      </div>
    </div>
  )
}
