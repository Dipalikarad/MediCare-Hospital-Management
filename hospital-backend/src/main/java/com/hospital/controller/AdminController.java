package com.hospital.controller;

import com.hospital.dto.request.*;
import com.hospital.dto.response.*;
import com.hospital.entity.Department;
import com.hospital.enums.AppointmentStatus;
import com.hospital.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

/**
 * Admin-only REST endpoints.
 * Secured at both SecurityConfig level and @PreAuthorize.
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final DoctorService      doctorService;
    private final PatientService     patientService;
    private final DepartmentService  departmentService;
    private final AppointmentService appointmentService;
    private final ReportService      reportService;

    // ═══════════════════════════════════════════════════════════
    // DASHBOARD
    // ═══════════════════════════════════════════════════════════

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<Map<String, Object>>> dashboard() {
        return ResponseEntity.ok(
            ApiResponse.success("Dashboard stats", reportService.getAdminDashboard())
        );
    }

    // ═══════════════════════════════════════════════════════════
    // DEPARTMENTS
    // ═══════════════════════════════════════════════════════════

    @PostMapping("/departments")
    public ResponseEntity<ApiResponse<Department>> createDepartment(
        @Valid @RequestBody DepartmentRequest req
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Department created", departmentService.create(req)));
    }

    @GetMapping("/departments")
    public ResponseEntity<ApiResponse<Page<Department>>> getDepartments(
        @RequestParam(defaultValue = "0")  int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success(
            "Departments", departmentService.getAll(PageRequest.of(page, size, Sort.by("name")))
        ));
    }

    @PutMapping("/departments/{id}")
    public ResponseEntity<ApiResponse<Department>> updateDepartment(
        @PathVariable Long id, @Valid @RequestBody DepartmentRequest req
    ) {
        return ResponseEntity.ok(
            ApiResponse.success("Department updated", departmentService.update(id, req))
        );
    }

    @DeleteMapping("/departments/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDepartment(@PathVariable Long id) {
        departmentService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Department deleted"));
    }

    // ═══════════════════════════════════════════════════════════
    // DOCTORS
    // ═══════════════════════════════════════════════════════════

    @PostMapping("/doctors")
    public ResponseEntity<ApiResponse<DoctorProfileResponse>> createDoctor(
        @Valid @RequestBody CreateDoctorRequest req
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Doctor created", doctorService.createDoctor(req)));
    }

    @GetMapping("/doctors")
    public ResponseEntity<ApiResponse<Page<DoctorProfileResponse>>> getDoctors(
        @RequestParam(required = false) String name,
        @RequestParam(required = false) Long   departmentId,
        @RequestParam(defaultValue = "0")  int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success(
            "Doctors",
            doctorService.searchDoctors(name, departmentId, PageRequest.of(page, size))
        ));
    }

    @GetMapping("/doctors/{profileId}")
    public ResponseEntity<ApiResponse<DoctorProfileResponse>> getDoctor(
        @PathVariable Long profileId
    ) {
        return ResponseEntity.ok(
            ApiResponse.success("Doctor", doctorService.getById(profileId))
        );
    }

    @PutMapping("/doctors/{profileId}")
    public ResponseEntity<ApiResponse<DoctorProfileResponse>> updateDoctor(
        @PathVariable Long profileId, @Valid @RequestBody DoctorProfileRequest req
    ) {
        return ResponseEntity.ok(
            ApiResponse.success("Doctor updated", doctorService.updateProfile(profileId, req))
        );
    }

    @PatchMapping("/doctors/{userId}/toggle-status")
    public ResponseEntity<ApiResponse<Void>> toggleDoctorStatus(@PathVariable Long userId) {
        doctorService.toggleStatus(userId);
        return ResponseEntity.ok(ApiResponse.success("Doctor status toggled"));
    }

    // ═══════════════════════════════════════════════════════════
    // PATIENTS
    // ═══════════════════════════════════════════════════════════

    @GetMapping("/patients")
    public ResponseEntity<ApiResponse<Page<PatientProfileResponse>>> getPatients(
        @RequestParam(defaultValue = "0")  int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success(
            "Patients", patientService.getAllPatients(PageRequest.of(page, size))
        ));
    }

    @PatchMapping("/patients/{userId}/toggle-status")
    public ResponseEntity<ApiResponse<Void>> togglePatientStatus(@PathVariable Long userId) {
        patientService.toggleStatus(userId);
        return ResponseEntity.ok(ApiResponse.success("Patient status toggled"));
    }

    // ═══════════════════════════════════════════════════════════
    // APPOINTMENTS
    // ═══════════════════════════════════════════════════════════

    @GetMapping("/appointments")
    public ResponseEntity<ApiResponse<Page<AppointmentResponse>>> getAppointments(
        @RequestParam(required = false) Long              doctorId,
        @RequestParam(required = false) AppointmentStatus status,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
        @RequestParam(defaultValue = "0")  int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success(
            "Appointments",
            appointmentService.getAllAppointments(
                doctorId, status, from, to,
                PageRequest.of(page, size, Sort.by("appointmentDate").descending())
            )
        ));
    }

    @GetMapping("/appointments/{id}")
    public ResponseEntity<ApiResponse<AppointmentResponse>> getAppointment(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Appointment", appointmentService.getById(id)));
    }

    // ═══════════════════════════════════════════════════════════
    // REPORTS
    // ═══════════════════════════════════════════════════════════

    @GetMapping("/reports/appointments-per-doctor")
    public ResponseEntity<ApiResponse<Map<String, Long>>> appointmentsPerDoctor(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ResponseEntity.ok(ApiResponse.success(
            "Report", reportService.appointmentsPerDoctor(from, to)
        ));
    }

    @GetMapping("/reports/appointments-per-department")
    public ResponseEntity<ApiResponse<Map<String, Long>>> appointmentsPerDepartment(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ResponseEntity.ok(ApiResponse.success(
            "Report", reportService.appointmentsPerDepartment(from, to)
        ));
    }

    @GetMapping("/reports/daily")
    public ResponseEntity<ApiResponse<Map<String, Long>>> dailyAppointments(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ResponseEntity.ok(ApiResponse.success(
            "Daily report", reportService.dailyAppointments(from, to)
        ));
    }
}
