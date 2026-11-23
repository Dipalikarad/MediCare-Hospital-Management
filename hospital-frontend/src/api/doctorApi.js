// src/api/doctorApi.js
import api from './axiosInstance'

export const getDoctorDashboard    = () => api.get('/api/doctor/dashboard')
export const getDoctorProfile      = () => api.get('/api/doctor/profile')
export const updateDoctorProfile   = (data) => api.put('/api/doctor/profile', data)

// Availability
export const getAvailability       = () => api.get('/api/doctor/availability')
export const addAvailability       = (data) => api.post('/api/doctor/availability', data)
export const updateAvailability    = (id, data) => api.put(`/api/doctor/availability/${id}`, data)
export const deleteAvailability    = (id) => api.delete(`/api/doctor/availability/${id}`)

// Appointments
export const getDoctorAppointments = (params) => api.get('/api/doctor/appointments', { params })
export const updateAppointmentStatus = (id, status) =>
  api.patch(`/api/doctor/appointments/${id}/status`, null, { params: { status } })

// Prescriptions
export const createPrescription    = (data) => api.post('/api/doctor/prescriptions', data)
export const getDoctorPrescriptions = (params) => api.get('/api/doctor/prescriptions', { params })

// Medical Records
export const createMedicalRecord   = (data) => api.post('/api/doctor/medical-records', data)
export const getPatientHistory     = (patientProfileId, params) =>
  api.get(`/api/doctor/patients/${patientProfileId}/history`, { params })
