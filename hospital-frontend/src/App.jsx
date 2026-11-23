// src/App.jsx
// Central router – public, admin, doctor, and patient routes

import { Routes, Route, Navigate } from 'react-router-dom'
import { useAuth } from './context/AuthContext'

import ProtectedRoute from './routes/ProtectedRoute'

// ── Layouts ──────────────────────────────────────────────────
import AdminLayout  from './layouts/AdminLayout'
import DoctorLayout from './layouts/DoctorLayout'
import PatientLayout from './layouts/PatientLayout'

// ── Auth pages ────────────────────────────────────────────────
import LoginPage         from './pages/auth/LoginPage'
import RegisterPage      from './pages/auth/RegisterPage'
import ForgotPasswordPage from './pages/auth/ForgotPasswordPage'
import ResetPasswordPage from './pages/auth/ResetPasswordPage'

// ── Admin pages ───────────────────────────────────────────────
import AdminDashboard    from './pages/admin/AdminDashboard'
import AdminDoctors      from './pages/admin/AdminDoctors'
import AdminPatients     from './pages/admin/AdminPatients'
import AdminDepartments  from './pages/admin/AdminDepartments'
import AdminAppointments from './pages/admin/AdminAppointments'
import AdminReports      from './pages/admin/AdminReports'

// ── Doctor pages ──────────────────────────────────────────────
import DoctorDashboard      from './pages/doctor/DoctorDashboard'
import DoctorAppointments   from './pages/doctor/DoctorAppointments'
import DoctorAvailability   from './pages/doctor/DoctorAvailability'
import DoctorPrescriptions  from './pages/doctor/DoctorPrescriptions'
import DoctorProfile        from './pages/doctor/DoctorProfile'

// ── Patient pages ─────────────────────────────────────────────
import PatientDashboard     from './pages/patient/PatientDashboard'
import PatientDoctors       from './pages/patient/PatientDoctors'
import BookAppointment      from './pages/patient/BookAppointment'
import PatientAppointments  from './pages/patient/PatientAppointments'
import PatientPrescriptions from './pages/patient/PatientPrescriptions'
import PatientHistory       from './pages/patient/PatientHistory'
import PatientProfilePage   from './pages/patient/PatientProfilePage'

// ── Home / 404 ────────────────────────────────────────────────
import HomePage    from './pages/HomePage'
import NotFound    from './pages/NotFound'

function RootRedirect() {
  const { user } = useAuth()
  if (!user) return <Navigate to="/login" replace />
  const map = { ADMIN: '/admin/dashboard', DOCTOR: '/doctor/dashboard', PATIENT: '/patient/dashboard' }
  return <Navigate to={map[user.role] || '/login'} replace />
}

export default function App() {
  return (
    <Routes>
      {/* ── Public ── */}
      <Route path="/"                 element={<HomePage />} />
      <Route path="/login"            element={<LoginPage />} />
      <Route path="/register"         element={<RegisterPage />} />
      <Route path="/forgot-password"  element={<ForgotPasswordPage />} />
      <Route path="/reset-password"   element={<ResetPasswordPage />} />
      <Route path="/home"             element={<RootRedirect />} />

      {/* ── Admin ── */}
      <Route element={<ProtectedRoute allowedRoles={['ADMIN']} />}>
        <Route element={<AdminLayout />}>
          <Route path="/admin/dashboard"    element={<AdminDashboard />} />
          <Route path="/admin/doctors"      element={<AdminDoctors />} />
          <Route path="/admin/patients"     element={<AdminPatients />} />
          <Route path="/admin/departments"  element={<AdminDepartments />} />
          <Route path="/admin/appointments" element={<AdminAppointments />} />
          <Route path="/admin/reports"      element={<AdminReports />} />
        </Route>
      </Route>

      {/* ── Doctor ── */}
      <Route element={<ProtectedRoute allowedRoles={['DOCTOR']} />}>
        <Route element={<DoctorLayout />}>
          <Route path="/doctor/dashboard"     element={<DoctorDashboard />} />
          <Route path="/doctor/appointments"  element={<DoctorAppointments />} />
          <Route path="/doctor/availability"  element={<DoctorAvailability />} />
          <Route path="/doctor/prescriptions" element={<DoctorPrescriptions />} />
          <Route path="/doctor/profile"       element={<DoctorProfile />} />
        </Route>
      </Route>

      {/* ── Patient ── */}
      <Route element={<ProtectedRoute allowedRoles={['PATIENT']} />}>
        <Route element={<PatientLayout />}>
          <Route path="/patient/dashboard"     element={<PatientDashboard />} />
          <Route path="/patient/doctors"       element={<PatientDoctors />} />
          <Route path="/patient/book"          element={<BookAppointment />} />
          <Route path="/patient/appointments"  element={<PatientAppointments />} />
          <Route path="/patient/prescriptions" element={<PatientPrescriptions />} />
          <Route path="/patient/history"       element={<PatientHistory />} />
          <Route path="/patient/profile"       element={<PatientProfilePage />} />
        </Route>
      </Route>

      {/* ── 404 ── */}
      <Route path="*" element={<NotFound />} />
    </Routes>
  )
}
