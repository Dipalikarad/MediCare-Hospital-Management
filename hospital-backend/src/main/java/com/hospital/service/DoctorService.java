package com.hospital.service;

import com.hospital.dto.request.CreateDoctorRequest;
import com.hospital.dto.request.DoctorProfileRequest;
import com.hospital.dto.response.DoctorProfileResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DoctorService {

    DoctorProfileResponse createDoctor(CreateDoctorRequest request);

    DoctorProfileResponse updateProfile(Long doctorProfileId, DoctorProfileRequest request);

    DoctorProfileResponse getByUserId(Long userId);

    DoctorProfileResponse getById(Long doctorProfileId);

    Page<DoctorProfileResponse> searchDoctors(String name, Long departmentId, Pageable pageable);

    void toggleStatus(Long userId); // admin activate/deactivate
}
