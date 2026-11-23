package com.hospital.service;

import com.hospital.dto.request.MedicalRecordRequest;
import com.hospital.dto.response.MedicalRecordResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MedicalRecordService {

    MedicalRecordResponse create(Long doctorUserId, MedicalRecordRequest request);

    Page<MedicalRecordResponse> getPatientHistory(Long requestingUserId, String role,
                                                   Long patientProfileId, Pageable pageable);

    Page<MedicalRecordResponse> getDoctorRecords(Long doctorUserId, Pageable pageable);
}
