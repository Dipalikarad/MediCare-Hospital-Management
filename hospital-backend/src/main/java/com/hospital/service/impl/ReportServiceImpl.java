package com.hospital.service.impl;

import com.hospital.enums.AppointmentStatus;
import com.hospital.enums.Role;
import com.hospital.repository.*;
import com.hospital.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

    private final AppointmentRepository   appointmentRepository;
    private final UserRepository          userRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final DepartmentRepository    departmentRepository;

    @Override
    public Map<String, Object> getAdminDashboard() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalDoctors",         userRepository.countByRole(Role.DOCTOR));
        stats.put("totalPatients",        userRepository.countByRole(Role.PATIENT));
        stats.put("totalAppointments",    appointmentRepository.count());
        stats.put("todayAppointments",    appointmentRepository.countByDate(LocalDate.now()));
        stats.put("pendingAppointments",  appointmentRepository.countByStatus(AppointmentStatus.PENDING));
        stats.put("completedAppointments",appointmentRepository.countByStatus(AppointmentStatus.COMPLETED));
        stats.put("cancelledAppointments",appointmentRepository.countByStatus(AppointmentStatus.CANCELLED));
        stats.put("totalDepartments",     departmentRepository.count());
        return stats;
    }

    @Override
    public Map<String, Long> appointmentsPerDoctor(LocalDate from, LocalDate to) {
        Map<String, Long> result = new LinkedHashMap<>();
        appointmentRepository.countPerDoctor(from, to).forEach(row ->
            doctorProfileRepository.findById((Long) row[0]).ifPresent(doc ->
                result.put(doc.getFullName(), (Long) row[1])
            )
        );
        return result;
    }

    @Override
    public Map<String, Long> appointmentsPerDepartment(LocalDate from, LocalDate to) {
        Map<String, Long> result = new LinkedHashMap<>();
        appointmentRepository.countPerDepartment(from, to).forEach(row ->
            departmentRepository.findById((Long) row[0]).ifPresent(dept ->
                result.put(dept.getName(), (Long) row[1])
            )
        );
        return result;
    }

    @Override
    public Map<String, Long> dailyAppointments(LocalDate from, LocalDate to) {
        Map<String, Long> result = new LinkedHashMap<>();
        appointmentRepository.dailyCounts(from, to).forEach(row ->
            result.put(row[0].toString(), (Long) row[1])
        );
        return result;
    }

    @Override
    public Map<String, Object> getDoctorDashboard(Long doctorUserId) {
        var doctorOpt = doctorProfileRepository.findByUserId(doctorUserId);
        if (doctorOpt.isEmpty()) return Collections.emptyMap();

        Long doctorId = doctorOpt.get().getId();
        LocalDate today = LocalDate.now();

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("todayAppointments",
            appointmentRepository.findByDoctorIdAndAppointmentDate(doctorId, today).size());
        stats.put("pendingCount",
            appointmentRepository.findAllFiltered(
                doctorId, AppointmentStatus.PENDING, null, null,
                org.springframework.data.domain.Pageable.unpaged()
            ).getTotalElements());
        stats.put("completedCount",
            appointmentRepository.findAllFiltered(
                doctorId, AppointmentStatus.COMPLETED, null, null,
                org.springframework.data.domain.Pageable.unpaged()
            ).getTotalElements());
        return stats;
    }
}
