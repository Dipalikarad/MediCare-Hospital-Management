package com.hospital.service;

import com.hospital.dto.request.AvailabilityRequest;
import com.hospital.dto.response.AvailabilityResponse;

import java.util.List;

public interface AvailabilityService {

    AvailabilityResponse save(Long doctorUserId, AvailabilityRequest request);

    List<AvailabilityResponse> getByDoctor(Long doctorProfileId);

    AvailabilityResponse update(Long doctorUserId, Long availabilityId, AvailabilityRequest request);

    void delete(Long doctorUserId, Long availabilityId);
}
