// src/api/adminApi.js
import api from './axiosInstance'

// Dashboard
export const getDashboard        = () => api.get('/api/admin/dashboard')

// Departments
export const getDepartments      = (p = 0, s = 50) => api.get(`/api/admin/departments?page=${p}&size=${s}`)
export const createDepartment    = (data) => api.post('/api/admin/departments', data)
export const updateDepartment    = (id, data) => api.put(`/api/admin/departments/${id}`, data)
export const deleteDepartment    = (id) => api.delete(`/api/admin/departments/${id}`)

// Doctors
export const getDoctors          = (params) => api.get('/api/admin/doctors', { params })
export const createDoctor        = (data) => api.post('/api/admin/doctors', data)
export const updateDoctor        = (id, data) => api.put(`/api/admin/doctors/${id}`, data)
export const toggleDoctorStatus  = (userId) => api.patch(`/api/admin/doctors/${userId}/toggle-status`)

// Patients
export const getPatients         = (params) => api.get('/api/admin/patients', { params })
export const togglePatientStatus = (userId) => api.patch(`/api/admin/patients/${userId}/toggle-status`)

// Appointments
export const getAllAppointments   = (params) => api.get('/api/admin/appointments', { params })

// Reports
export const getAppointmentsPerDoctor     = (from, to) =>
  api.get('/api/admin/reports/appointments-per-doctor', { params: { from, to } })
export const getAppointmentsPerDepartment = (from, to) =>
  api.get('/api/admin/reports/appointments-per-department', { params: { from, to } })
export const getDailyAppointments         = (from, to) =>
  api.get('/api/admin/reports/daily', { params: { from, to } })
