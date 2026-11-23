package com.hospital.service;

import java.time.LocalDate;
import java.util.Map;

/** Aggregated report data for dashboards. */
public interface ReportService {

    /** Admin dashboard: total users, today's appointments, pending count, etc. */
    Map<String, Object> getAdminDashboard();

    /** Appointments per doctor in a date range. */
    Map<String, Long> appointmentsPerDoctor(LocalDate from, LocalDate to);

    /** Appointments per department in a date range. */
    Map<String, Long> appointmentsPerDepartment(LocalDate from, LocalDate to);

    /** Daily appointment counts. */
    Map<String, Long> dailyAppointments(LocalDate from, LocalDate to);

    /** Doctor-specific dashboard. */
    Map<String, Object> getDoctorDashboard(Long doctorUserId);
}
