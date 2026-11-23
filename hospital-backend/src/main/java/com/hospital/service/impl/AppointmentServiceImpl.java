package com.hospital.service.impl;

import com.hospital.dto.request.*;
import com.hospital.dto.response.AppointmentResponse;
import com.hospital.dto.response.SlotResponse;
import com.hospital.entity.*;
import com.hospital.enums.AppointmentStatus;
import com.hospital.exception.*;
import com.hospital.repository.*;
import com.hospital.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/**
 * Core appointment business logic.
 * All 10 business rules from the spec are enforced here.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository      appointmentRepository;
    private final PatientProfileRepository   patientProfileRepository;
    private final DoctorProfileRepository    doctorProfileRepository;
    private final DoctorAvailabilityRepository availabilityRepository;

    // ═══════════════════════════════════════════════════════════
    // PATIENT ACTIONS
    // ═══════════════════════════════════════════════════════════

    @Override
    public AppointmentResponse bookAppointment(Long patientUserId, BookAppointmentRequest req) {
        PatientProfile patient = getPatientByUserId(patientUserId);
        DoctorProfile  doctor  = getDoctorById(req.getDoctorId());

        // Rule 3: cannot book in the past
        if (req.getAppointmentDate().isBefore(LocalDate.now())) {
            throw new BusinessException("Cannot book an appointment in the past.");
        }

        // Rule 4: validate the date falls within doctor availability
        int dayOfWeek = req.getAppointmentDate().getDayOfWeek().getValue(); // 1=Mon, 7=Sun
        DoctorAvailability avail = availabilityRepository
            .findByDoctorIdAndDayOfWeek(doctor.getId(), dayOfWeek)
            .filter(DoctorAvailability::getActive)
            .orElseThrow(() -> new BusinessException(
                "Doctor is not available on " + req.getAppointmentDate().getDayOfWeek()
            ));

        // Calculate slot end time
        LocalTime startTime = req.getStartTime();
        LocalTime endTime   = startTime.plusMinutes(avail.getSlotDurationMinutes());

        // Validate the requested slot is within availability window
        if (startTime.isBefore(avail.getStartTime()) || endTime.isAfter(avail.getEndTime())) {
            throw new BusinessException(
                "Requested time is outside doctor availability window: "
                + avail.getStartTime() + " – " + avail.getEndTime()
            );
        }

        // Rule 1: doctor double-booking check (also enforced by DB unique constraint)
        if (appointmentRepository.existsByDoctorIdAndAppointmentDateAndStartTime(
            doctor.getId(), req.getAppointmentDate(), startTime)) {
            throw new BusinessException("This time slot is already booked. Please choose another.");
        }

        // Rule 10: patient overlap check
        if (appointmentRepository.hasPatientOverlap(
            patient.getId(), req.getAppointmentDate(), startTime, endTime)) {
            throw new BusinessException("You already have an appointment overlapping this time.");
        }

        Appointment appointment = Appointment.builder()
            .patient(patient)
            .doctor(doctor)
            .appointmentDate(req.getAppointmentDate())
            .startTime(startTime)
            .endTime(endTime)
            .status(AppointmentStatus.PENDING)
            .reason(req.getReason())
            .build();

        return toResponse(appointmentRepository.save(appointment));
    }

    @Override
    public AppointmentResponse cancelAppointment(
        Long patientUserId, Long appointmentId, CancelAppointmentRequest req
    ) {
        PatientProfile patient = getPatientByUserId(patientUserId);
        Appointment appt = appointmentRepository.findByIdAndPatientId(appointmentId, patient.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        // Rule 5a: only PENDING or CONFIRMED can be cancelled
        if (!EnumSet.of(AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED)
                .contains(appt.getStatus())) {
            throw new BusinessException(
                "Only PENDING or CONFIRMED appointments can be cancelled."
            );
        }

        // Rule 5b: not within 2 hours of start
        LocalDateTime startDateTime = appt.getAppointmentDate().atTime(appt.getStartTime());
        if (LocalDateTime.now().isAfter(startDateTime.minusHours(2))) {
            throw new BusinessException(
                "Cannot cancel within 2 hours of the appointment start time."
            );
        }

        appt.setStatus(AppointmentStatus.CANCELLED);
        appt.setCancelReason(req.getCancelReason());
        return toResponse(appointmentRepository.save(appt));
    }

    @Override
    public AppointmentResponse rescheduleAppointment(
        Long patientUserId, Long appointmentId, RescheduleRequest req
    ) {
        PatientProfile patient = getPatientByUserId(patientUserId);
        Appointment appt = appointmentRepository.findByIdAndPatientId(appointmentId, patient.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        // Can only reschedule PENDING or CONFIRMED
        if (!EnumSet.of(AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED)
                .contains(appt.getStatus())) {
            throw new BusinessException("Only PENDING or CONFIRMED appointments can be rescheduled.");
        }
        if (req.getNewDate().isBefore(LocalDate.now())) {
            throw new BusinessException("Cannot reschedule to the past.");
        }

        DoctorProfile doctor = appt.getDoctor();
        int dayOfWeek = req.getNewDate().getDayOfWeek().getValue();
        DoctorAvailability avail = availabilityRepository
            .findByDoctorIdAndDayOfWeek(doctor.getId(), dayOfWeek)
            .filter(DoctorAvailability::getActive)
            .orElseThrow(() -> new BusinessException(
                "Doctor is not available on " + req.getNewDate().getDayOfWeek()
            ));

        LocalTime newEnd = req.getNewStartTime().plusMinutes(avail.getSlotDurationMinutes());

        if (req.getNewStartTime().isBefore(avail.getStartTime()) || newEnd.isAfter(avail.getEndTime())) {
            throw new BusinessException("Requested time is outside doctor's availability.");
        }

        if (appointmentRepository.existsByDoctorIdAndAppointmentDateAndStartTime(
            doctor.getId(), req.getNewDate(), req.getNewStartTime())) {
            throw new BusinessException("That time slot is already taken.");
        }

        appt.setAppointmentDate(req.getNewDate());
        appt.setStartTime(req.getNewStartTime());
        appt.setEndTime(newEnd);
        appt.setStatus(AppointmentStatus.PENDING); // reset to pending on reschedule
        appt.setReason(req.getReason() != null ? req.getReason() : appt.getReason());

        return toResponse(appointmentRepository.save(appt));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AppointmentResponse> getPatientAppointments(
        Long patientUserId, AppointmentStatus status, Pageable pageable
    ) {
        PatientProfile patient = getPatientByUserId(patientUserId);
        if (status != null) {
            return appointmentRepository
                .findByPatientIdAndStatus(patient.getId(), status, pageable)
                .map(this::toResponse);
        }
        return appointmentRepository.findByPatientId(patient.getId(), pageable).map(this::toResponse);
    }

    // ═══════════════════════════════════════════════════════════
    // DOCTOR ACTIONS
    // ═══════════════════════════════════════════════════════════

    @Override
    @Transactional(readOnly = true)
    public Page<AppointmentResponse> getDoctorAppointments(
        Long doctorUserId, AppointmentStatus status, Pageable pageable
    ) {
        DoctorProfile doctor = getDoctorByUserId(doctorUserId);
        if (status != null) {
            // Filter on the full findByDoctorId result since we need status filter
            return appointmentRepository.findAllFiltered(
                doctor.getId(), status, null, null, pageable
            ).map(this::toResponse);
        }
        return appointmentRepository.findByDoctorId(doctor.getId(), pageable).map(this::toResponse);
    }

    @Override
    public AppointmentResponse updateStatus(
        Long doctorUserId, Long appointmentId, AppointmentStatus newStatus
    ) {
        DoctorProfile doctor = getDoctorByUserId(doctorUserId);

        // Rule 6: only the assigned doctor can change status
        Appointment appt = appointmentRepository.findByIdAndDoctorId(appointmentId, doctor.getId())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Appointment not found or you are not the assigned doctor"
            ));

        validateStatusTransition(appt.getStatus(), newStatus);
        appt.setStatus(newStatus);
        return toResponse(appointmentRepository.save(appt));
    }

    // ═══════════════════════════════════════════════════════════
    // SLOT CALCULATION
    // ═══════════════════════════════════════════════════════════

    @Override
    @Transactional(readOnly = true)
    public List<SlotResponse> getAvailableSlots(Long doctorProfileId, LocalDate date) {
        DoctorProfile doctor = getDoctorById(doctorProfileId);

        int dayOfWeek = date.getDayOfWeek().getValue();
        Optional<DoctorAvailability> availOpt = availabilityRepository
            .findByDoctorIdAndDayOfWeek(doctor.getId(), dayOfWeek)
            .filter(DoctorAvailability::getActive);

        if (availOpt.isEmpty()) return Collections.emptyList();

        DoctorAvailability avail = availOpt.get();

        // Get already booked start times
        List<LocalTime> bookedTimes = appointmentRepository
            .findByDoctorIdAndAppointmentDate(doctorProfileId, date)
            .stream()
            .filter(a -> !EnumSet.of(AppointmentStatus.CANCELLED, AppointmentStatus.NO_SHOW)
                .contains(a.getStatus()))
            .map(Appointment::getStartTime)
            .toList();

        // Generate all slots
        List<SlotResponse> slots = new ArrayList<>();
        LocalTime cursor = avail.getStartTime();
        while (cursor.plusMinutes(avail.getSlotDurationMinutes())
                .compareTo(avail.getEndTime()) <= 0) {
            SlotResponse slot = new SlotResponse();
            slot.setStartTime(cursor);
            slot.setEndTime(cursor.plusMinutes(avail.getSlotDurationMinutes()));
            slot.setAvailable(!bookedTimes.contains(cursor));
            // Also mark past slots as unavailable for today
            if (date.isEqual(LocalDate.now()) && cursor.isBefore(LocalTime.now())) {
                slot.setAvailable(false);
            }
            slots.add(slot);
            cursor = cursor.plusMinutes(avail.getSlotDurationMinutes());
        }
        return slots;
    }

    // ═══════════════════════════════════════════════════════════
    // ADMIN
    // ═══════════════════════════════════════════════════════════

    @Override
    @Transactional(readOnly = true)
    public Page<AppointmentResponse> getAllAppointments(
        Long doctorId, AppointmentStatus status,
        LocalDate from, LocalDate to, Pageable pageable
    ) {
        return appointmentRepository
            .findAllFiltered(doctorId, status, from, to, pageable)
            .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public AppointmentResponse getById(Long id) {
        return appointmentRepository.findById(id)
            .map(this::toResponse)
            .orElseThrow(() -> new ResourceNotFoundException("Appointment not found: " + id));
    }

    // ═══════════════════════════════════════════════════════════
    // HELPERS
    // ═══════════════════════════════════════════════════════════

    private void validateStatusTransition(AppointmentStatus current, AppointmentStatus next) {
        boolean valid = switch (next) {
            case CONFIRMED  -> current == AppointmentStatus.PENDING;
            case COMPLETED  -> current == AppointmentStatus.CONFIRMED;
            case NO_SHOW    -> current == AppointmentStatus.CONFIRMED;
            case CANCELLED  -> EnumSet.of(AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED).contains(current);
            default         -> false;
        };
        if (!valid) {
            throw new BusinessException(
                "Cannot transition from " + current + " to " + next
            );
        }
    }

    private PatientProfile getPatientByUserId(Long userId) {
        return patientProfileRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found"));
    }

    private DoctorProfile getDoctorByUserId(Long userId) {
        return doctorProfileRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found"));
    }

    private DoctorProfile getDoctorById(Long id) {
        return doctorProfileRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Doctor not found: " + id));
    }

    private AppointmentResponse toResponse(Appointment a) {
        AppointmentResponse r = new AppointmentResponse();
        r.setId(a.getId());
        r.setPatientId(a.getPatient().getId());
        r.setPatientName(a.getPatient().getFullName());
        r.setDoctorId(a.getDoctor().getId());
        r.setDoctorName(a.getDoctor().getFullName());
        r.setDepartmentName(a.getDoctor().getDepartment().getName());
        r.setAppointmentDate(a.getAppointmentDate());
        r.setStartTime(a.getStartTime());
        r.setEndTime(a.getEndTime());
        r.setStatus(a.getStatus());
        r.setReason(a.getReason());
        r.setCancelReason(a.getCancelReason());
        r.setCreatedAt(a.getCreatedAt());
        return r;
    }
}
