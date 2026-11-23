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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Patient-only REST endpoints.
 */
@RestController
@RequestMapping("/api/patient")
@PreAuthorize("hasRole('PATIENT')")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService     patientService;
    private final AppointmentService appointmentService;
    private final PrescriptionService prescriptionService;
    private final MedicalRecordService medicalRecordService;
    private final DoctorService      doctorService;
    private final UserRepository     userRepository;

    private Long currentUserId() {
        String email = SecurityUtils.getCurrentUserEmail();
        return userRepository.findByEmail(email).orElseThrow().getId();
    }

    // ═══════════════════════════════════════════════════════════
    // PROFILE
    // ═══════════════════════════════════════════════════════════

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<PatientProfileResponse>> getProfile() {
        return ResponseEntity.ok(
            ApiResponse.success("Profile", patientService.getByUserId(currentUserId()))
        );
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<PatientProfileResponse>> updateProfile(
        @Valid @RequestBody PatientProfileRequest req
    ) {
        return ResponseEntity.ok(
            ApiResponse.success("Profile updated",
                patientService.updateProfile(currentUserId(), req))
        );
    }

    // ═══════════════════════════════════════════════════════════
    // BROWSE DOCTORS
    // ═══════════════════════════════════════════════════════════

    @GetMapping("/doctors")
    public ResponseEntity<ApiResponse<Page<DoctorProfileResponse>>> searchDoctors(
        @RequestParam(required = false) String name,
        @RequestParam(required = false) Long   departmentId,
        @RequestParam(defaultValue = "0")  int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success(
            "Doctors", doctorService.searchDoctors(name, departmentId, PageRequest.of(page, size))
        ));
    }

    @GetMapping("/doctors/{profileId}")
    public ResponseEntity<ApiResponse<DoctorProfileResponse>> getDoctorProfile(
        @PathVariable Long profileId
    ) {
        return ResponseEntity.ok(
            ApiResponse.success("Doctor", doctorService.getById(profileId))
        );
    }

    /** GET /api/patient/doctors/{profileId}/slots?date=2024-12-25 */
    @GetMapping("/doctors/{profileId}/slots")
    public ResponseEntity<ApiResponse<List<SlotResponse>>> getSlots(
        @PathVariable Long profileId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ResponseEntity.ok(ApiResponse.success(
            "Available slots", appointmentService.getAvailableSlots(profileId, date)
        ));
    }

    // ═══════════════════════════════════════════════════════════
    // APPOINTMENTS
    // ═══════════════════════════════════════════════════════════

    @PostMapping("/appointments")
    public ResponseEntity<ApiResponse<AppointmentResponse>> bookAppointment(
        @Valid @RequestBody BookAppointmentRequest req
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Appointment booked",
                appointmentService.bookAppointment(currentUserId(), req)));
    }

    @GetMapping("/appointments")
    public ResponseEntity<ApiResponse<Page<AppointmentResponse>>> getMyAppointments(
        @RequestParam(required = false) AppointmentStatus status,
        @RequestParam(defaultValue = "0")  int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success(
            "Appointments",
            appointmentService.getPatientAppointments(
                currentUserId(), status,
                PageRequest.of(page, size, Sort.by("appointmentDate").descending())
            )
        ));
    }

    @PatchMapping("/appointments/{id}/cancel")
    public ResponseEntity<ApiResponse<AppointmentResponse>> cancelAppointment(
        @PathVariable Long id,
        @Valid @RequestBody CancelAppointmentRequest req
    ) {
        return ResponseEntity.ok(ApiResponse.success(
            "Appointment cancelled",
            appointmentService.cancelAppointment(currentUserId(), id, req)
        ));
    }

    @PatchMapping("/appointments/{id}/reschedule")
    public ResponseEntity<ApiResponse<AppointmentResponse>> rescheduleAppointment(
        @PathVariable Long id,
        @Valid @RequestBody RescheduleRequest req
    ) {
        return ResponseEntity.ok(ApiResponse.success(
            "Appointment rescheduled",
            appointmentService.rescheduleAppointment(currentUserId(), id, req)
        ));
    }

    // ═══════════════════════════════════════════════════════════
    // PRESCRIPTIONS
    // ═══════════════════════════════════════════════════════════

    @GetMapping("/prescriptions")
    public ResponseEntity<ApiResponse<Page<PrescriptionResponse>>> getMyPrescriptions(
        @RequestParam(defaultValue = "0")  int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success(
            "Prescriptions",
            prescriptionService.getPatientPrescriptions(
                currentUserId(), PageRequest.of(page, size, Sort.by("createdAt").descending())
            )
        ));
    }

    // ═══════════════════════════════════════════════════════════
    // MEDICAL HISTORY
    // ═══════════════════════════════════════════════════════════

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<Page<MedicalRecordResponse>>> getMyHistory(
        @RequestParam(defaultValue = "0")  int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        Long userId = currentUserId();
        // Fetch profile ID
        PatientProfileResponse profile = patientService.getByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(
            "Medical history",
            medicalRecordService.getPatientHistory(
                userId, "ROLE_PATIENT", profile.getId(),
                PageRequest.of(page, size, Sort.by("recordDate").descending())
            )
        ));
    }
}
