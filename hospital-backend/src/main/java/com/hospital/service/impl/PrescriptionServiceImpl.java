package com.hospital.service.impl;

import com.hospital.dto.request.PrescriptionRequest;
import com.hospital.dto.response.PrescriptionResponse;
import com.hospital.entity.*;
import com.hospital.enums.AppointmentStatus;
import com.hospital.exception.*;
import com.hospital.repository.*;
import com.hospital.service.PrescriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PrescriptionServiceImpl implements PrescriptionService {

    private final PrescriptionRepository   prescriptionRepository;
    private final AppointmentRepository    appointmentRepository;
    private final DoctorProfileRepository  doctorProfileRepository;
    private final PatientProfileRepository patientProfileRepository;

    @Override
    public PrescriptionResponse create(Long doctorUserId, PrescriptionRequest req) {
        // Rule 7: prescription only for COMPLETED appointment
        Appointment appt = appointmentRepository.findById(req.getAppointmentId())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Appointment not found: " + req.getAppointmentId()
            ));

        if (appt.getStatus() != AppointmentStatus.COMPLETED) {
            throw new BusinessException(
                "Prescription can only be created for a COMPLETED appointment."
            );
        }

        // Rule 7: only the assigned doctor
        DoctorProfile doctor = doctorProfileRepository.findByUserId(doctorUserId)
            .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found"));

        if (!appt.getDoctor().getId().equals(doctor.getId())) {
            throw new AccessDeniedException(
                "Only the assigned doctor can create a prescription for this appointment."
            );
        }

        // Rule 7: one prescription per appointment
        if (prescriptionRepository.existsByAppointmentId(appt.getId())) {
            throw new BusinessException("A prescription already exists for this appointment.");
        }

        Prescription prescription = Prescription.builder()
            .appointment(appt)
            .doctor(doctor)
            .patient(appt.getPatient())
            .diagnosis(req.getDiagnosis())
            .advice(req.getAdvice())
            .build();

        for (PrescriptionRequest.ItemRequest itemReq : req.getItems()) {
            PrescriptionItem item = PrescriptionItem.builder()
                .medicineName(itemReq.getMedicineName())
                .dosage(itemReq.getDosage())
                .frequency(itemReq.getFrequency())
                .durationDays(itemReq.getDurationDays())
                .instructions(itemReq.getInstructions())
                .build();
            prescription.addItem(item);
        }

        return toResponse(prescriptionRepository.save(prescription));
    }

    @Override
    @Transactional(readOnly = true)
    public PrescriptionResponse getByAppointmentId(Long appointmentId) {
        return prescriptionRepository.findByAppointmentId(appointmentId)
            .map(this::toResponse)
            .orElseThrow(() -> new ResourceNotFoundException(
                "No prescription found for appointment: " + appointmentId
            ));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PrescriptionResponse> getPatientPrescriptions(Long patientUserId, Pageable pageable) {
        PatientProfile patient = patientProfileRepository.findByUserId(patientUserId)
            .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found"));
        return prescriptionRepository.findByPatientId(patient.getId(), pageable)
            .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PrescriptionResponse> getDoctorPrescriptions(Long doctorUserId, Pageable pageable) {
        DoctorProfile doctor = doctorProfileRepository.findByUserId(doctorUserId)
            .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found"));
        return prescriptionRepository.findByDoctorId(doctor.getId(), pageable)
            .map(this::toResponse);
    }

    // ── Mapping ───────────────────────────────────────────────

    private PrescriptionResponse toResponse(Prescription p) {
        PrescriptionResponse r = new PrescriptionResponse();
        r.setId(p.getId());
        r.setAppointmentId(p.getAppointment().getId());
        r.setDoctorId(p.getDoctor().getId());
        r.setDoctorName(p.getDoctor().getFullName());
        r.setPatientId(p.getPatient().getId());
        r.setPatientName(p.getPatient().getFullName());
        r.setDiagnosis(p.getDiagnosis());
        r.setAdvice(p.getAdvice());
        r.setCreatedAt(p.getCreatedAt());

        List<PrescriptionResponse.PrescriptionItemResponse> items = p.getItems().stream()
            .map(i -> {
                PrescriptionResponse.PrescriptionItemResponse ir =
                    new PrescriptionResponse.PrescriptionItemResponse();
                ir.setId(i.getId());
                ir.setMedicineName(i.getMedicineName());
                ir.setDosage(i.getDosage());
                ir.setFrequency(i.getFrequency());
                ir.setDurationDays(i.getDurationDays());
                ir.setInstructions(i.getInstructions());
                return ir;
            }).toList();
        r.setItems(items);
        return r;
    }
}
