package com.hospital.controller;

import com.hospital.dto.request.*;
import com.hospital.dto.response.*;
import com.hospital.enums.AppointmentStatus;
import com.hospital.repository.UserRepository;
import com.hospital.service.*;
import com.hospital.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Doctor-only REST endpoints.
 */
@RestController
@RequestMapping("/api/doctor")
@PreAuthorize("hasRole('DOCTOR')")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService      doctorService;
    private final AppointmentService appointmentService;
    private final AvailabilityService availabilityService;
    private final PrescriptionService prescriptionService;
    private final MedicalRecordService medicalRecordService;
    private final ReportService      reportService;
    private final UserRepository     userRepository;

    // ── Helper: get current logged-in userId ──────────────────
    private Long currentUserId() {
        String email = SecurityUtils.getCurrentUserEmail();
        return userRepository.findByEmail(email)
            .orElseThrow().getId();
    }

    // ═══════════════════════════════════════════════════════════
    // DASHBOARD
    // ═══════════════════════════════════════════════════════════

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<Map<String, Object>>> dashboard() {
        return ResponseEntity.ok(
            ApiResponse.success("Dashboard", reportService.getDoctorDashboard(currentUserId()))
        );
    }

    // ═══════════════════════════════════════════════════════════
    // PROFILE
    // ═══════════════════════════════════════════════════════════

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<DoctorProfileResponse>> getProfile() {
        return ResponseEntity.ok(
            ApiResponse.success("Profile", doctorService.getByUserId(currentUserId()))
        );
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<DoctorProfileResponse>> updateProfile(
        @Valid @RequestBody DoctorProfileRequest req
    ) {
        DoctorProfileResponse profile = doctorService.getByUserId(currentUserId());
        return ResponseEntity.ok(
            ApiResponse.success("Profile updated",
                doctorService.updateProfile(profile.getId(), req))
        );
    }

    // ═══════════════════════════════════════════════════════════
    // AVAILABILITY
    // ═══════════════════════════════════════════════════════════

    @PostMapping("/availability")
    public ResponseEntity<ApiResponse<AvailabilityResponse>> addAvailability(
        @Valid @RequestBody AvailabilityRequest req
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Availability added",
                availabilityService.save(currentUserId(), req)));
    }

    @GetMapping("/availability")
    public ResponseEntity<ApiResponse<List<AvailabilityResponse>>> getAvailability() {
        DoctorProfileResponse profile = doctorService.getByUserId(currentUserId());
        return ResponseEntity.ok(
            ApiResponse.success("Availability",
                availabilityService.getByDoctor(profile.getId()))
        );
    }

    @PutMapping("/availability/{id}")
    public ResponseEntity<ApiResponse<AvailabilityResponse>> updateAvailability(
        @PathVariable Long id, @Valid @RequestBody AvailabilityRequest req
    ) {
        return ResponseEntity.ok(
            ApiResponse.success("Updated",
                availabilityService.update(currentUserId(), id, req))
        );
    }

    @DeleteMapping("/availability/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAvailability(@PathVariable Long id) {
        availabilityService.delete(currentUserId(), id);
        return ResponseEntity.ok(ApiResponse.success("Deleted"));
    }

    // ═══════════════════════════════════════════════════════════
    // APPOINTMENTS
    // ═══════════════════════════════════════════════════════════

    @GetMapping("/appointments")
    public ResponseEntity<ApiResponse<Page<AppointmentResponse>>> getAppointments(
        @RequestParam(required = false) AppointmentStatus status,
        @RequestParam(defaultValue = "0")  int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success(
            "Appointments",
            appointmentService.getDoctorAppointments(
                currentUserId(), status,
                PageRequest.of(page, size, Sort.by("appointmentDate").descending())
            )
        ));
    }

    /** PATCH /api/doctor/appointments/{id}/status?status=CONFIRMED */
    @PatchMapping("/appointments/{id}/status")
    public ResponseEntity<ApiResponse<AppointmentResponse>> updateAppointmentStatus(
        @PathVariable Long id,
        @RequestParam AppointmentStatus status
    ) {
        return ResponseEntity.ok(
            ApiResponse.success("Status updated",
                appointmentService.updateStatus(currentUserId(), id, status))
        );
    }

    // ═══════════════════════════════════════════════════════════
    // PRESCRIPTIONS
    // ═══════════════════════════════════════════════════════════

    @PostMapping("/prescriptions")
    public ResponseEntity<ApiResponse<PrescriptionResponse>> createPrescription(
        @Valid @RequestBody PrescriptionRequest req
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Prescription created",
                prescriptionService.create(currentUserId(), req)));
    }

    @GetMapping("/prescriptions")
    public ResponseEntity<ApiResponse<Page<PrescriptionResponse>>> getPrescriptions(
        @RequestParam(defaultValue = "0")  int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success(
            "Prescriptions",
            prescriptionService.getDoctorPrescriptions(
                currentUserId(), PageRequest.of(page, size, Sort.by("createdAt").descending())
            )
        ));
    }

    // ═══════════════════════════════════════════════════════════
    // MEDICAL RECORDS
    // ═══════════════════════════════════════════════════════════

    @PostMapping("/medical-records")
    public ResponseEntity<ApiResponse<MedicalRecordResponse>> createRecord(
        @Valid @RequestBody MedicalRecordRequest req
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Record created",
                medicalRecordService.create(currentUserId(), req)));
    }

    /** View medical history of a specific patient (must have prior appointment). */
    @GetMapping("/patients/{patientProfileId}/history")
    public ResponseEntity<ApiResponse<Page<MedicalRecordResponse>>> getPatientHistory(
        @PathVariable Long patientProfileId,
        @RequestParam(defaultValue = "0")  int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success(
            "Patient history",
            medicalRecordService.getPatientHistory(
                currentUserId(), "ROLE_DOCTOR", patientProfileId,
                PageRequest.of(page, size, Sort.by("recordDate").descending())
            )
        ));
    }
}
