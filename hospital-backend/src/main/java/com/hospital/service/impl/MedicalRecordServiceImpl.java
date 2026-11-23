package com.hospital.service.impl;

import com.hospital.dto.request.MedicalRecordRequest;
import com.hospital.dto.response.MedicalRecordResponse;
import com.hospital.entity.*;
import com.hospital.exception.*;
import com.hospital.repository.*;
import com.hospital.service.MedicalRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MedicalRecordServiceImpl implements MedicalRecordService {

    private final MedicalRecordRepository  medicalRecordRepository;
    private final DoctorProfileRepository  doctorProfileRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final AppointmentRepository    appointmentRepository;

    @Override
    public MedicalRecordResponse create(Long doctorUserId, MedicalRecordRequest req) {
        DoctorProfile doctor = doctorProfileRepository.findByUserId(doctorUserId)
            .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found"));

        PatientProfile patient = patientProfileRepository.findById(req.getPatientId())
            .orElseThrow(() -> new ResourceNotFoundException("Patient not found: " + req.getPatientId()));

        // Rule 8: doctor can only record for patients who had an appointment with them
        boolean hasRelation = appointmentRepository
            .hasPatientAppointmentWithDoctor(patient.getId(), doctor.getId());
        if (!hasRelation) {
            throw new AccessDeniedException(
                "You can only create records for patients who have had appointments with you."
            );
        }

        Appointment appointment = null;
        if (req.getAppointmentId() != null) {
            appointment = appointmentRepository.findById(req.getAppointmentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Appointment not found: " + req.getAppointmentId()
                ));
        }

        MedicalRecord record = MedicalRecord.builder()
            .patient(patient)
            .doctor(doctor)
            .appointment(appointment)
            .diagnosis(req.getDiagnosis())
            .symptoms(req.getSymptoms())
            .treatmentNotes(req.getTreatmentNotes())
            .recordDate(req.getRecordDate())
            .build();

        return toResponse(medicalRecordRepository.save(record));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MedicalRecordResponse> getPatientHistory(
        Long requestingUserId, String role, Long patientProfileId, Pageable pageable
    ) {
        // Rule 8 access control
        if (role.equals("ROLE_PATIENT")) {
            PatientProfile self = patientProfileRepository.findByUserId(requestingUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));
            if (!self.getId().equals(patientProfileId)) {
                throw new AccessDeniedException("You can only view your own medical history.");
            }
        } else if (role.equals("ROLE_DOCTOR")) {
            DoctorProfile doctor = doctorProfileRepository.findByUserId(requestingUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));
            boolean hasRelation = appointmentRepository
                .hasPatientAppointmentWithDoctor(patientProfileId, doctor.getId());
            if (!hasRelation) {
                throw new AccessDeniedException(
                    "You can only view history of patients who had appointments with you."
                );
            }
        }
        // ADMIN: no restriction
        return medicalRecordRepository.findByPatientId(patientProfileId, pageable)
            .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MedicalRecordResponse> getDoctorRecords(Long doctorUserId, Pageable pageable) {
        DoctorProfile doctor = doctorProfileRepository.findByUserId(doctorUserId)
            .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found"));
        return medicalRecordRepository.findByDoctorId(doctor.getId(), pageable)
            .map(this::toResponse);
    }

    private MedicalRecordResponse toResponse(MedicalRecord m) {
        MedicalRecordResponse r = new MedicalRecordResponse();
        r.setId(m.getId());
        r.setPatientId(m.getPatient().getId());
        r.setPatientName(m.getPatient().getFullName());
        r.setDoctorId(m.getDoctor().getId());
        r.setDoctorName(m.getDoctor().getFullName());
        r.setAppointmentId(m.getAppointment() != null ? m.getAppointment().getId() : null);
        r.setDiagnosis(m.getDiagnosis());
        r.setSymptoms(m.getSymptoms());
        r.setTreatmentNotes(m.getTreatmentNotes());
        r.setRecordDate(m.getRecordDate());
        return r;
    }
}
