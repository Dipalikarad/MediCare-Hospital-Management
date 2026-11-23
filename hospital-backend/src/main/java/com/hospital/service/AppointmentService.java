package com.hospital.service;

import com.hospital.dto.request.*;
import com.hospital.dto.response.AppointmentResponse;
import com.hospital.dto.response.SlotResponse;
import com.hospital.enums.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface AppointmentService {

    // ── Patient actions ───────────────────────────────────────
    AppointmentResponse bookAppointment(Long patientUserId, BookAppointmentRequest request);

    AppointmentResponse cancelAppointment(Long patientUserId, Long appointmentId,
                                          CancelAppointmentRequest request);

    AppointmentResponse rescheduleAppointment(Long patientUserId, Long appointmentId,
                                              RescheduleRequest request);

    Page<AppointmentResponse> getPatientAppointments(Long patientUserId,
                                                     AppointmentStatus status,
                                                     Pageable pageable);

    // ── Doctor actions ────────────────────────────────────────
    Page<AppointmentResponse> getDoctorAppointments(Long doctorUserId,
                                                    AppointmentStatus status,
                                                    Pageable pageable);

    AppointmentResponse updateStatus(Long doctorUserId, Long appointmentId,
                                     AppointmentStatus newStatus);

    // ── Slot calculation ──────────────────────────────────────
    List<SlotResponse> getAvailableSlots(Long doctorProfileId, LocalDate date);

    // ── Admin ─────────────────────────────────────────────────
    Page<AppointmentResponse> getAllAppointments(Long doctorId, AppointmentStatus status,
                                                 LocalDate from, LocalDate to, Pageable pageable);

    AppointmentResponse getById(Long id);
}
