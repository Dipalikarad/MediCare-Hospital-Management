// src/api/authApi.js
import api from './axiosInstance'

export const registerApi  = (data) => api.post('/api/auth/register', data)
export const loginApi     = (data) => api.post('/api/auth/login', data)
export const googleLoginApi = (idToken) => api.post('/api/auth/google', { idToken })
export const forgotPasswordApi = (data) => api.post('/api/auth/forgot-password', data)
export const resetPasswordApi  = (data) => api.post('/api/auth/reset-password', data)
