// src/context/AuthContext.jsx
// Global authentication state: JWT decode, role, auto-logout on expiry

import { createContext, useContext, useState, useEffect, useCallback } from 'react'
import { jwtDecode } from 'jwt-decode'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser]       = useState(null)   // { userId, email, role, exp }
  const [token, setToken]     = useState(null)
  const [loading, setLoading] = useState(true)   // while re-hydrating from localStorage

  const logout = useCallback(() => {
    localStorage.removeItem('token')
    setToken(null)
    setUser(null)
  }, [])

  // ── Re-hydrate on mount ───────────────────────────────────
  useEffect(() => {
    const storedToken = localStorage.getItem('token')
    if (storedToken) {
      try {
        const decoded = jwtDecode(storedToken)
        if (decoded.exp * 1000 > Date.now()) {
          setToken(storedToken)
          setUser({ userId: decoded.userId, email: decoded.sub, role: decoded.role })
        } else {
          // Expired – clear storage
          localStorage.removeItem('token')
        }
      } catch {
        localStorage.removeItem('token')
      }
    }
    setLoading(false)
  }, [])

  // ── Auto-logout timer ────────────────────────────────────
  useEffect(() => {
    if (!token) return
    const decoded = jwtDecode(token)
    const msUntilExpiry = decoded.exp * 1000 - Date.now()
    if (msUntilExpiry <= 0) { logout(); return }
    const timer = setTimeout(logout, msUntilExpiry)
    return () => clearTimeout(timer)
  }, [token, logout])

  // ── Login ─────────────────────────────────────────────────
  const login = useCallback((jwtAuthResponse) => {
    const { accessToken, userId, email, role } = jwtAuthResponse
    localStorage.setItem('token', accessToken)
    setToken(accessToken)
    setUser({ userId, email, role })
  }, [])

  // ── Logout ────────────────────────────────────────────────
  const isAuthenticated = !!user
  const isAdmin   = user?.role === 'ADMIN'
  const isDoctor  = user?.role === 'DOCTOR'
  const isPatient = user?.role === 'PATIENT'

  return (
    <AuthContext.Provider value={{
      user, token, loading,
      login, logout,
      isAuthenticated, isAdmin, isDoctor, isPatient
    }}>
      {children}
    </AuthContext.Provider>
  )
}

// Hook for consuming auth context
export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within an AuthProvider')
  return ctx
}
