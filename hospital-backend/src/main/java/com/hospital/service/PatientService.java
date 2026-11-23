package com.hospital.service;

import com.hospital.dto.request.PatientProfileRequest;
import com.hospital.dto.response.PatientProfileResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PatientService {

    PatientProfileResponse getByUserId(Long userId);

    PatientProfileResponse updateProfile(Long userId, PatientProfileRequest request);

    Page<PatientProfileResponse> getAllPatients(Pageable pageable);

    void toggleStatus(Long userId); // admin activate/deactivate
}
