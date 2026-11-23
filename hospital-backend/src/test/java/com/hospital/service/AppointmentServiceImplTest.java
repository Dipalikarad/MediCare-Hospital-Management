package com.hospital.service;

import com.hospital.dto.request.BookAppointmentRequest;
import com.hospital.dto.response.AppointmentResponse;
import com.hospital.entity.*;
import com.hospital.enums.AppointmentStatus;
import com.hospital.exception.BusinessException;
import com.hospital.repository.*;
import com.hospital.service.impl.AppointmentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AppointmentServiceImpl.
 * Tests all 10 business rules from the specification.
 */
@ExtendWith(MockitoExtension.class)
class AppointmentServiceImplTest {

    @Mock private AppointmentRepository appointmentRepository;
    @Mock private PatientProfileRepository patientProfileRepository;
    @Mock private DoctorProfileRepository  doctorProfileRepository;
    @Mock private DoctorAvailabilityRepository availabilityRepository;

    @InjectMocks
    private AppointmentServiceImpl service;

    private PatientProfile patient;
    private DoctorProfile  doctor;
    private Department     dept;
    private DoctorAvailability availability;

    @BeforeEach
    void setUp() {
        dept = Department.builder().id(1L).name("Cardiology").build();

        User userDoctor = User.builder().id(2L).email("dr@test.com").build();
        doctor = DoctorProfile.builder()
            .id(1L).user(userDoctor).fullName("Dr. Test").employeeId("E001")
            .department(dept).consultationFee(new BigDecimal("500"))
            .build();

        User userPatient = User.builder().id(3L).email("patient@test.com").build();
        patient = PatientProfile.builder()
            .id(1L).user(userPatient).fullName("Test Patient")
            .build();

        availability = DoctorAvailability.builder()
            .id(1L).doctor(doctor).dayOfWeek(1) // Monday
            .startTime(LocalTime.of(9, 0))
            .endTime(LocalTime.of(17, 0))
            .slotDurationMinutes(30)
            .active(true)
            .build();
    }

    // ═══════════════════════════════════════════════════════════
    // Rule 1: No double-booking
    // ═══════════════════════════════════════════════════════════

    @Test
    @DisplayName("Rule 1: Should reject double-booking for same doctor, date, and time")
    void shouldRejectDoubleBooking() {
        BookAppointmentRequest req = new BookAppointmentRequest();
        req.setDoctorId(1L);
        req.setAppointmentDate(LocalDate.of(2026, 11, 30)); // Monday
        req.setStartTime(LocalTime.of(10, 0));
        req.setReason("Checkup");

        when(patientProfileRepository.findByUserId(3L)).thenReturn(Optional.of(patient));
        when(doctorProfileRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(availabilityRepository.findByDoctorIdAndDayOfWeek(1L, 1))
            .thenReturn(Optional.of(availability));
        when(appointmentRepository.existsByDoctorIdAndAppointmentDateAndStartTime(
            eq(1L), any(), any())).thenReturn(true); // Already booked!

        assertThatThrownBy(() -> service.bookAppointment(3L, req))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("already booked");
    }

    // ═══════════════════════════════════════════════════════════
    // Rule 2: Booking within doctor's availability
    // ═══════════════════════════════════════════════════════════

    @Test
    @DisplayName("Rule 2: Should reject booking outside doctor's availability window")
    void shouldRejectOutsideAvailability() {
        BookAppointmentRequest req = new BookAppointmentRequest();
        req.setDoctorId(1L);
        req.setAppointmentDate(LocalDate.of(2026, 11, 30)); // Monday
        req.setStartTime(LocalTime.of(7, 0)); // Before 9:00 AM
        req.setReason("Early visit");

        when(patientProfileRepository.findByUserId(3L)).thenReturn(Optional.of(patient));
        when(doctorProfileRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(availabilityRepository.findByDoctorIdAndDayOfWeek(1L, 1))
            .thenReturn(Optional.of(availability));

        assertThatThrownBy(() -> service.bookAppointment(3L, req))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("outside doctor availability");
    }

    // ═══════════════════════════════════════════════════════════
    // Rule 3: No booking in the past
    // ═══════════════════════════════════════════════════════════

    @Test
    @DisplayName("Rule 3: Should reject booking in the past")
    void shouldRejectPastBooking() {
        BookAppointmentRequest req = new BookAppointmentRequest();
        req.setDoctorId(1L);
        req.setAppointmentDate(LocalDate.of(2020, 1, 1));
        req.setStartTime(LocalTime.of(10, 0));

        when(patientProfileRepository.findByUserId(3L)).thenReturn(Optional.of(patient));
        when(doctorProfileRepository.findById(1L)).thenReturn(Optional.of(doctor));

        assertThatThrownBy(() -> service.bookAppointment(3L, req))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("past");
    }

    // ═══════════════════════════════════════════════════════════
    // Rule 4: No booking on days without availability
    // ═══════════════════════════════════════════════════════════

    @Test
    @DisplayName("Rule 4: Should reject booking on a day with no availability (holiday)")
    void shouldRejectHolidayBooking() {
        BookAppointmentRequest req = new BookAppointmentRequest();
        req.setDoctorId(1L);
        req.setAppointmentDate(LocalDate.of(2026, 11, 29)); // Sunday
        req.setStartTime(LocalTime.of(10, 0));

        when(patientProfileRepository.findByUserId(3L)).thenReturn(Optional.of(patient));
        when(doctorProfileRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(availabilityRepository.findByDoctorIdAndDayOfWeek(1L, 7))
            .thenReturn(Optional.empty()); // No availability on Sunday

        assertThatThrownBy(() -> service.bookAppointment(3L, req))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("not available");
    }

    // ═══════════════════════════════════════════════════════════
    // Rule 5: Patient cancellation constraints
    // ═══════════════════════════════════════════════════════════

    @Test
    @DisplayName("Rule 5a: Should reject cancelling a COMPLETED appointment")
    void shouldRejectCancellingCompletedAppointment() {
        Appointment appt = Appointment.builder()
            .id(1L).patient(patient).doctor(doctor)
            .appointmentDate(LocalDate.now().plusDays(2))
            .startTime(LocalTime.of(10, 0)).endTime(LocalTime.of(10, 30))
            .status(AppointmentStatus.COMPLETED)
            .build();

        when(patientProfileRepository.findByUserId(3L)).thenReturn(Optional.of(patient));
        when(appointmentRepository.findByIdAndPatientId(1L, 1L)).thenReturn(Optional.of(appt));

        assertThatThrownBy(() ->
            service.cancelAppointment(3L, 1L,
                new com.hospital.dto.request.CancelAppointmentRequest()))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("PENDING or CONFIRMED");
    }

    // ═══════════════════════════════════════════════════════════
    // Rule 6: Only assigned doctor can change status
    // ═══════════════════════════════════════════════════════════

    @Test
    @DisplayName("Rule 6: Should reject status change by non-assigned doctor")
    void shouldRejectStatusChangeByWrongDoctor() {
        when(doctorProfileRepository.findByUserId(99L)).thenReturn(Optional.of(
            DoctorProfile.builder().id(99L)
                .user(User.builder().id(99L).build())
                .fullName("Dr. Other").employeeId("E099")
                .department(dept).build()
        ));
        when(appointmentRepository.findByIdAndDoctorId(1L, 99L))
            .thenReturn(Optional.empty()); // Not this doctor's appointment

        assertThatThrownBy(() ->
            service.updateStatus(99L, 1L, AppointmentStatus.CONFIRMED))
            .isInstanceOf(com.hospital.exception.ResourceNotFoundException.class);
    }

    // ═══════════════════════════════════════════════════════════
    // Rule 10: Patient overlap check
    // ═══════════════════════════════════════════════════════════

    @Test
    @DisplayName("Rule 10: Should reject overlapping patient appointment")
    void shouldRejectOverlappingPatientAppointment() {
        BookAppointmentRequest req = new BookAppointmentRequest();
        req.setDoctorId(1L);
        req.setAppointmentDate(LocalDate.of(2026, 11, 30)); // Monday
        req.setStartTime(LocalTime.of(10, 0));

        when(patientProfileRepository.findByUserId(3L)).thenReturn(Optional.of(patient));
        when(doctorProfileRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(availabilityRepository.findByDoctorIdAndDayOfWeek(1L, 1))
            .thenReturn(Optional.of(availability));
        when(appointmentRepository.existsByDoctorIdAndAppointmentDateAndStartTime(
            anyLong(), any(), any())).thenReturn(false);
        when(appointmentRepository.hasPatientOverlap(
            eq(1L), any(), any(), any())).thenReturn(true); // Patient already has overlapping appt

        assertThatThrownBy(() -> service.bookAppointment(3L, req))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("overlapping");
    }

    // ═══════════════════════════════════════════════════════════
    // Successful booking
    // ═══════════════════════════════════════════════════════════

    @Test
    @DisplayName("Should successfully book an appointment when all rules pass")
    void shouldBookSuccessfully() {
        BookAppointmentRequest req = new BookAppointmentRequest();
        req.setDoctorId(1L);
        req.setAppointmentDate(LocalDate.of(2026, 11, 30)); // Monday
        req.setStartTime(LocalTime.of(10, 0));
        req.setReason("Routine checkup");

        Appointment saved = Appointment.builder()
            .id(1L).patient(patient).doctor(doctor)
            .appointmentDate(req.getAppointmentDate())
            .startTime(LocalTime.of(10, 0)).endTime(LocalTime.of(10, 30))
            .status(AppointmentStatus.PENDING).reason("Routine checkup")
            .build();

        when(patientProfileRepository.findByUserId(3L)).thenReturn(Optional.of(patient));
        when(doctorProfileRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(availabilityRepository.findByDoctorIdAndDayOfWeek(1L, 1))
            .thenReturn(Optional.of(availability));
        when(appointmentRepository.existsByDoctorIdAndAppointmentDateAndStartTime(
            anyLong(), any(), any())).thenReturn(false);
        when(appointmentRepository.hasPatientOverlap(
            anyLong(), any(), any(), any())).thenReturn(false);
        when(appointmentRepository.save(any())).thenReturn(saved);

        AppointmentResponse resp = service.bookAppointment(3L, req);

        assertThat(resp).isNotNull();
        assertThat(resp.getStatus()).isEqualTo(AppointmentStatus.PENDING);
        assertThat(resp.getDoctorName()).isEqualTo("Dr. Test");
        assertThat(resp.getPatientName()).isEqualTo("Test Patient");
        verify(appointmentRepository).save(any(Appointment.class));
    }

    // ═══════════════════════════════════════════════════════════
    // Status transition validation
    // ═══════════════════════════════════════════════════════════

    @Test
    @DisplayName("Should reject invalid status transitions")
    void shouldRejectInvalidTransition() {
        Appointment appt = Appointment.builder()
            .id(1L).patient(patient).doctor(doctor)
            .appointmentDate(LocalDate.now().plusDays(1))
            .startTime(LocalTime.of(10, 0)).endTime(LocalTime.of(10, 30))
            .status(AppointmentStatus.PENDING)
            .build();

        when(doctorProfileRepository.findByUserId(2L)).thenReturn(Optional.of(doctor));
        when(appointmentRepository.findByIdAndDoctorId(1L, 1L)).thenReturn(Optional.of(appt));

        // PENDING -> COMPLETED is invalid (must go through CONFIRMED first)
        assertThatThrownBy(() ->
            service.updateStatus(2L, 1L, AppointmentStatus.COMPLETED))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Cannot transition");
    }
}
