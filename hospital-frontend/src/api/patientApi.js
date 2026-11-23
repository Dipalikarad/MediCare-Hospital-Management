// src/api/patientApi.js
import api from './axiosInstance'

export const getPatientProfile    = () => api.get('/api/patient/profile')
export const updatePatientProfile = (data) => api.put('/api/patient/profile', data)

// Doctors
export const searchDoctors        = (params) => api.get('/api/patient/doctors', { params })
export const getDoctorDetail      = (id) => api.get(`/api/patient/doctors/${id}`)
export const getDoctorSlots       = (profileId, date) =>
  api.get(`/api/patient/doctors/${profileId}/slots`, { params: { date } })

// Appointments
export const bookAppointment      = (data) => api.post('/api/patient/appointments', data)
export const getMyAppointments    = (params) => api.get('/api/patient/appointments', { params })
export const cancelAppointment    = (id, data) => api.patch(`/api/patient/appointments/${id}/cancel`, data)
export const rescheduleAppointment = (id, data) => api.patch(`/api/patient/appointments/${id}/reschedule`, data)

// Prescriptions
export const getMyPrescriptions   = (params) => api.get('/api/patient/prescriptions', { params })

// History
export const getMyHistory         = (params) => api.get('/api/patient/history', { params })

// Public departments
export const getDepartments       = () => api.get('/api/departments')
