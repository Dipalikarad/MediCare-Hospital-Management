// src/api/axiosInstance.js
// Axios instance with JWT interceptor and 401/403 handling

import axios from 'axios'
import { toast } from 'react-toastify'

// Use the Vite /api proxy by default so localhost and 127.0.0.1 both work
// without browser CORS issues. Set VITE_API_BASE_URL only for a deployed API.
const BASE_URL = import.meta.env.VITE_API_BASE_URL?.trim() || ''

const api = axios.create({
  baseURL: BASE_URL,
  headers: { 'Content-Type': 'application/json' },
  timeout: 15000
})

// ── Request interceptor: attach JWT ──────────────────────────
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// ── Response interceptor: handle 401/403 ─────────────────────
api.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error?.response?.status
    const message = error?.response?.data?.message || 'An error occurred'

    if (status === 401) {
      // Token expired or invalid – clear auth and redirect to login
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      toast.error('Session expired. Please log in again.')
      window.location.href = '/login'
    } else if (status === 403) {
      toast.error('You do not have permission to perform this action.')
    } else if (status >= 500) {
      toast.error('Server error. Please try again later.')
    } else if (status) {
      toast.error(message)
    }

    return Promise.reject(error)
  }
)

export default api
