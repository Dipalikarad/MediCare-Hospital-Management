package com.hospital.service.impl;

import com.hospital.dto.request.AvailabilityRequest;
import com.hospital.dto.response.AvailabilityResponse;
import com.hospital.entity.DoctorAvailability;
import com.hospital.entity.DoctorProfile;
import com.hospital.exception.AccessDeniedException;
import com.hospital.exception.BusinessException;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.repository.DoctorAvailabilityRepository;
import com.hospital.repository.DoctorProfileRepository;
import com.hospital.service.AvailabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AvailabilityServiceImpl implements AvailabilityService {

    private final DoctorAvailabilityRepository availabilityRepository;
    private final DoctorProfileRepository      doctorProfileRepository;

    @Override
    public AvailabilityResponse save(Long doctorUserId, AvailabilityRequest req) {
        DoctorProfile doctor = getByUserId(doctorUserId);
        validateTimes(req);

        DoctorAvailability avail = DoctorAvailability.builder()
            .doctor(doctor)
            .dayOfWeek(req.getDayOfWeek())
            .startTime(req.getStartTime())
            .endTime(req.getEndTime())
            .slotDurationMinutes(req.getSlotDurationMinutes())
            .active(req.getActive() != null ? req.getActive() : true)
            .build();

        return toResponse(availabilityRepository.save(avail));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AvailabilityResponse> getByDoctor(Long doctorProfileId) {
        return availabilityRepository.findByDoctorId(doctorProfileId)
            .stream().map(this::toResponse).toList();
    }

    @Override
    public AvailabilityResponse update(Long doctorUserId, Long availId, AvailabilityRequest req) {
        DoctorProfile doctor = getByUserId(doctorUserId);
        DoctorAvailability avail = availabilityRepository.findById(availId)
            .orElseThrow(() -> new ResourceNotFoundException("Availability not found: " + availId));

        // Ownership check
        if (!avail.getDoctor().getId().equals(doctor.getId())) {
            throw new AccessDeniedException("You can only modify your own availability");
        }
        validateTimes(req);

        avail.setDayOfWeek(req.getDayOfWeek());
        avail.setStartTime(req.getStartTime());
        avail.setEndTime(req.getEndTime());
        avail.setSlotDurationMinutes(req.getSlotDurationMinutes());
        avail.setActive(req.getActive() != null ? req.getActive() : avail.getActive());

        return toResponse(availabilityRepository.save(avail));
    }

    @Override
    public void delete(Long doctorUserId, Long availId) {
        DoctorProfile doctor = getByUserId(doctorUserId);
        DoctorAvailability avail = availabilityRepository.findById(availId)
            .orElseThrow(() -> new ResourceNotFoundException("Availability not found"));

        if (!avail.getDoctor().getId().equals(doctor.getId())) {
            throw new AccessDeniedException("You can only delete your own availability");
        }
        availabilityRepository.delete(avail);
    }

    // ── Helpers ───────────────────────────────────────────────

    private DoctorProfile getByUserId(Long userId) {
        return doctorProfileRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found"));
    }

    private void validateTimes(AvailabilityRequest req) {
        if (!req.getEndTime().isAfter(req.getStartTime())) {
            throw new BusinessException("End time must be after start time");
        }
    }

    private AvailabilityResponse toResponse(DoctorAvailability a) {
        AvailabilityResponse r = new AvailabilityResponse();
        r.setId(a.getId());
        r.setDayOfWeek(a.getDayOfWeek());
        r.setDayName(DayOfWeek.of(a.getDayOfWeek()).name());
        r.setStartTime(a.getStartTime());
        r.setEndTime(a.getEndTime());
        r.setSlotDurationMinutes(a.getSlotDurationMinutes());
        r.setActive(a.getActive());
        return r;
    }
}
