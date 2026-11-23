// src/pages/auth/ResetPasswordPage.jsx
import { useForm } from 'react-hook-form'
import { yupResolver } from '@hookform/resolvers/yup'
import * as yup from 'yup'
import { useNavigate, useSearchParams, Link } from 'react-router-dom'
import { toast } from 'react-toastify'
import { resetPasswordApi } from '../../api/authApi'

const schema = yup.object({
  newPassword: yup.string().min(8, 'At least 8 characters').required(),
  confirm:     yup.string().oneOf([yup.ref('newPassword')], 'Passwords must match').required()
})

export default function ResetPasswordPage() {
  const [params]  = useSearchParams()
  const navigate  = useNavigate()
  const token     = params.get('token')

  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm({
    resolver: yupResolver(schema)
  })

  const onSubmit = async ({ newPassword }) => {
    try {
      await resetPasswordApi({ token, newPassword })
      toast.success('Password reset successfully! Please log in.')
      navigate('/login')
    } catch (err) {
      toast.error(err?.response?.data?.message || 'Reset failed. Token may be expired.')
    }
  }

  if (!token) {
    return (
      <div className="auth-container">
        <div className="auth-card card p-4 text-center">
          <i className="bi bi-x-circle fs-1 text-danger"></i>
          <p className="mt-3">Invalid or missing reset token.</p>
          <Link to="/forgot-password" className="btn btn-primary">Request New Link</Link>
        </div>
      </div>
    )
  }

  return (
    <div className="auth-container">
      <div className="auth-card card p-4 p-md-5">
        <div className="text-center mb-4">
          <i className="bi bi-shield-lock fs-1 text-success"></i>
          <h4 className="fw-bold mt-2">Set New Password</h4>
        </div>
        <form onSubmit={handleSubmit(onSubmit)} noValidate>
          <div className="mb-3">
            <label className="form-label fw-semibold">New Password</label>
            <input type="password"
              className={`form-control ${errors.newPassword ? 'is-invalid' : ''}`}
              placeholder="Min 8 characters" {...register('newPassword')} />
            {errors.newPassword && <div className="invalid-feedback">{errors.newPassword.message}</div>}
          </div>
          <div className="mb-3">
            <label className="form-label fw-semibold">Confirm Password</label>
            <input type="password"
              className={`form-control ${errors.confirm ? 'is-invalid' : ''}`}
              placeholder="Repeat password" {...register('confirm')} />
            {errors.confirm && <div className="invalid-feedback">{errors.confirm.message}</div>}
          </div>
          <button type="submit" className="btn btn-success w-100" disabled={isSubmitting}>
            {isSubmitting && <span className="spinner-border spinner-border-sm me-2"></span>}
            Reset Password
          </button>
        </form>
      </div>
    </div>
  )
}
