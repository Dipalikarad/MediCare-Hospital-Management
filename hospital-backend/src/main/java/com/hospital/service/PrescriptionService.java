package com.hospital.service;

import com.hospital.dto.request.PrescriptionRequest;
import com.hospital.dto.response.PrescriptionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PrescriptionService {

    PrescriptionResponse create(Long doctorUserId, PrescriptionRequest request);

    PrescriptionResponse getByAppointmentId(Long appointmentId);

    Page<PrescriptionResponse> getPatientPrescriptions(Long patientUserId, Pageable pageable);

    Page<PrescriptionResponse> getDoctorPrescriptions(Long doctorUserId, Pageable pageable);
}
