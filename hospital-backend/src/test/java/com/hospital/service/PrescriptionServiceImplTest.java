package com.hospital.service;

import com.hospital.dto.request.PrescriptionRequest;
import com.hospital.entity.*;
import com.hospital.enums.AppointmentStatus;
import com.hospital.exception.AccessDeniedException;
import com.hospital.exception.BusinessException;
import com.hospital.repository.*;
import com.hospital.service.impl.PrescriptionServiceImpl;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PrescriptionServiceImpl.
 * Validates business rules 7: prescription constraints.
 */
@ExtendWith(MockitoExtension.class)
class PrescriptionServiceImplTest {

    @Mock private PrescriptionRepository   prescriptionRepository;
    @Mock private AppointmentRepository    appointmentRepository;
    @Mock private DoctorProfileRepository  doctorProfileRepository;
    @Mock private PatientProfileRepository patientProfileRepository;

    @InjectMocks
    private PrescriptionServiceImpl service;

    private DoctorProfile  doctor;
    private PatientProfile patient;
    private Appointment    completedAppt;

    @BeforeEach
    void setUp() {
        Department dept = Department.builder().id(1L).name("Cardiology").build();

        User uDoctor  = User.builder().id(2L).email("dr@test.com").build();
        doctor  = DoctorProfile.builder().id(1L).user(uDoctor).fullName("Dr. Test")
            .employeeId("E001").department(dept).consultationFee(new BigDecimal("500")).build();

        User uPatient = User.builder().id(3L).email("patient@test.com").build();
        patient = PatientProfile.builder().id(1L).user(uPatient).fullName("Test Patient").build();

        completedAppt = Appointment.builder()
            .id(1L).patient(patient).doctor(doctor)
            .appointmentDate(LocalDate.now()).startTime(LocalTime.of(10, 0))
            .endTime(LocalTime.of(10, 30)).status(AppointmentStatus.COMPLETED)
            .build();
    }

    @Test
    @DisplayName("Rule 7a: Should reject prescription for non-COMPLETED appointment")
    void shouldRejectForNonCompletedAppointment() {
        Appointment pendingAppt = Appointment.builder()
            .id(2L).patient(patient).doctor(doctor)
            .status(AppointmentStatus.PENDING)
            .build();

        PrescriptionRequest req = new PrescriptionRequest();
        req.setAppointmentId(2L);
        req.setDiagnosis("Test");
        PrescriptionRequest.ItemRequest item = new PrescriptionRequest.ItemRequest();
        item.setMedicineName("Aspirin");
        req.setItems(List.of(item));

        when(appointmentRepository.findById(2L)).thenReturn(Optional.of(pendingAppt));

        assertThatThrownBy(() -> service.create(2L, req))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("COMPLETED");
    }

    @Test
    @DisplayName("Rule 7b: Should reject prescription by non-assigned doctor")
    void shouldRejectPrescriptionByWrongDoctor() {
        DoctorProfile otherDoctor = DoctorProfile.builder().id(99L)
            .user(User.builder().id(99L).build())
            .fullName("Dr. Other").employeeId("E099")
            .department(Department.builder().id(1L).name("Test").build())
            .build();

        PrescriptionRequest req = new PrescriptionRequest();
        req.setAppointmentId(1L);
        req.setDiagnosis("Test");
        PrescriptionRequest.ItemRequest item = new PrescriptionRequest.ItemRequest();
        item.setMedicineName("Test Med");
        req.setItems(List.of(item));

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(completedAppt));
        when(doctorProfileRepository.findByUserId(99L)).thenReturn(Optional.of(otherDoctor));

        assertThatThrownBy(() -> service.create(99L, req))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessageContaining("assigned doctor");
    }

    @Test
    @DisplayName("Rule 7c: Should reject duplicate prescription for same appointment")
    void shouldRejectDuplicatePrescription() {
        PrescriptionRequest req = new PrescriptionRequest();
        req.setAppointmentId(1L);
        req.setDiagnosis("Test");
        PrescriptionRequest.ItemRequest item = new PrescriptionRequest.ItemRequest();
        item.setMedicineName("Aspirin");
        req.setItems(List.of(item));

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(completedAppt));
        when(doctorProfileRepository.findByUserId(2L)).thenReturn(Optional.of(doctor));
        when(prescriptionRepository.existsByAppointmentId(1L)).thenReturn(true); // Already exists

        assertThatThrownBy(() -> service.create(2L, req))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("already exists");
    }

    @Test
    @DisplayName("Should create prescription successfully for valid request")
    void shouldCreatePrescriptionSuccessfully() {
        PrescriptionRequest req = new PrescriptionRequest();
        req.setAppointmentId(1L);
        req.setDiagnosis("Hypertension");
        req.setAdvice("Reduce salt");
        PrescriptionRequest.ItemRequest item = new PrescriptionRequest.ItemRequest();
        item.setMedicineName("Amlodipine");
        item.setDosage("5mg");
        item.setFrequency("Once daily");
        item.setDurationDays(30);
        req.setItems(List.of(item));

        Prescription saved = Prescription.builder()
            .id(1L).appointment(completedAppt).doctor(doctor).patient(patient)
            .diagnosis("Hypertension").advice("Reduce salt")
            .build();
        PrescriptionItem savedItem = PrescriptionItem.builder()
            .id(1L).prescription(saved).medicineName("Amlodipine")
            .dosage("5mg").frequency("Once daily").durationDays(30)
            .build();
        saved.setItems(List.of(savedItem));

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(completedAppt));
        when(doctorProfileRepository.findByUserId(2L)).thenReturn(Optional.of(doctor));
        when(prescriptionRepository.existsByAppointmentId(1L)).thenReturn(false);
        when(prescriptionRepository.save(any())).thenReturn(saved);

        var resp = service.create(2L, req);

        assertThat(resp).isNotNull();
        assertThat(resp.getDiagnosis()).isEqualTo("Hypertension");
        assertThat(resp.getItems()).hasSize(1);
        assertThat(resp.getItems().get(0).getMedicineName()).isEqualTo("Amlodipine");
        verify(prescriptionRepository).save(any(Prescription.class));
    }
}
