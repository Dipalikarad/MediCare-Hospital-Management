// src/pages/admin/AdminReports.jsx  (Phase 9 – recharts)
import { useState } from 'react'
import { toast } from 'react-toastify'
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend,
  LineChart, Line, ResponsiveContainer, PieChart, Pie, Cell
} from 'recharts'
import { getAppointmentsPerDoctor, getAppointmentsPerDepartment, getDailyAppointments } from '../../api/adminApi'
import { format, subDays } from 'date-fns'

const COLORS = ['#0d6efd','#198754','#ffc107','#dc3545','#0dcaf0','#6610f2','#fd7e14','#20c997']

export default function AdminReports() {
  const today    = format(new Date(), 'yyyy-MM-dd')
  const lastMonth = format(subDays(new Date(), 30), 'yyyy-MM-dd')

  const [from, setFrom]        = useState(lastMonth)
  const [to, setTo]            = useState(today)
  const [doctorData, setDrData]  = useState([])
  const [deptData, setDeptData]  = useState([])
  const [dailyData, setDaily]    = useState([])
  const [loading, setLoading]    = useState(false)

  const loadReports = async () => {
    setLoading(true)
    try {
      const [dr, dep, daily] = await Promise.all([
        getAppointmentsPerDoctor(from, to),
        getAppointmentsPerDepartment(from, to),
        getDailyAppointments(from, to)
      ])
      setDrData(Object.entries(dr.data.data || {}).map(([name, count]) => ({ name, count })))
      setDeptData(Object.entries(dep.data.data || {}).map(([name, count]) => ({ name, count })))
      setDaily(Object.entries(daily.data.data || {}).map(([date, count]) => ({ date, count })))
    } catch { toast.error('Failed to load reports') }
    setLoading(false)
  }

  return (
    <div>
      <h4 className="fw-bold mb-4"><i className="bi bi-bar-chart-line me-2 text-primary"></i>Reports & Analytics</h4>

      {/* Date range picker */}
      <div className="card shadow-sm p-3 mb-4">
        <div className="row g-3 align-items-end">
          <div className="col-md-4">
            <label className="form-label small fw-semibold">From</label>
            <input type="date" className="form-control" value={from} onChange={e => setFrom(e.target.value)} />
          </div>
          <div className="col-md-4">
            <label className="form-label small fw-semibold">To</label>
            <input type="date" className="form-control" value={to} onChange={e => setTo(e.target.value)} />
          </div>
          <div className="col-md-4">
            <button className="btn btn-primary w-100" onClick={loadReports} disabled={loading}>
              {loading ? <span className="spinner-border spinner-border-sm me-2"></span> : <i className="bi bi-graph-up me-2"></i>}
              Generate Reports
            </button>
          </div>
        </div>
      </div>

      {/* Daily appointments line chart */}
      {dailyData.length > 0 && (
        <div className="card shadow-sm p-4 mb-4">
          <h6 className="fw-semibold mb-3">Daily Appointments</h6>
          <ResponsiveContainer width="100%" height={260}>
            <LineChart data={dailyData}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="date" tick={{ fontSize: 11 }} />
              <YAxis allowDecimals={false} />
              <Tooltip />
              <Legend />
              <Line type="monotone" dataKey="count" stroke="#0d6efd" strokeWidth={2} dot={{ r: 3 }} name="Appointments" />
            </LineChart>
          </ResponsiveContainer>
        </div>
      )}

      <div className="row g-4">
        {/* Per doctor bar chart */}
        {doctorData.length > 0 && (
          <div className="col-lg-6">
            <div className="card shadow-sm p-4 h-100">
              <h6 className="fw-semibold mb-3">Appointments per Doctor</h6>
              <ResponsiveContainer width="100%" height={260}>
                <BarChart data={doctorData} layout="vertical">
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis type="number" allowDecimals={false} />
                  <YAxis type="category" dataKey="name" width={120} tick={{ fontSize: 11 }} />
                  <Tooltip />
                  <Bar dataKey="count" fill="#0d6efd" name="Appointments" />
                </BarChart>
              </ResponsiveContainer>
            </div>
          </div>
        )}

        {/* Per department pie chart */}
        {deptData.length > 0 && (
          <div className="col-lg-6">
            <div className="card shadow-sm p-4 h-100">
              <h6 className="fw-semibold mb-3">Appointments per Department</h6>
              <ResponsiveContainer width="100%" height={260}>
                <PieChart>
                  <Pie data={deptData} dataKey="count" nameKey="name"
                    cx="50%" cy="50%" outerRadius={100} label={({ name, percent }) =>
                      `${name} ${(percent * 100).toFixed(0)}%`
                    }>
                    {deptData.map((_, i) => <Cell key={i} fill={COLORS[i % COLORS.length]} />)}
                  </Pie>
                  <Tooltip />
                </PieChart>
              </ResponsiveContainer>
            </div>
          </div>
        )}
      </div>

      {!loading && doctorData.length === 0 && deptData.length === 0 && dailyData.length === 0 && (
        <div className="text-center text-muted py-5">
          <i className="bi bi-bar-chart fs-1 opacity-25"></i>
          <p className="mt-2">Select a date range and click &quot;Generate Reports&quot;</p>
        </div>
      )}
    </div>
  )
}
