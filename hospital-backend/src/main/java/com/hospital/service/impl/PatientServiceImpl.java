package com.hospital.service.impl;

import com.hospital.dto.request.PatientProfileRequest;
import com.hospital.dto.response.PatientProfileResponse;
import com.hospital.entity.PatientProfile;
import com.hospital.entity.User;
import com.hospital.enums.AccountStatus;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.repository.PatientProfileRepository;
import com.hospital.repository.UserRepository;
import com.hospital.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PatientServiceImpl implements PatientService {

    private final PatientProfileRepository patientProfileRepository;
    private final UserRepository           userRepository;

    @Override
    @Transactional(readOnly = true)
    public PatientProfileResponse getByUserId(Long userId) {
        return patientProfileRepository.findByUserId(userId)
            .map(this::toResponse)
            .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found for user " + userId));
    }

    @Override
    public PatientProfileResponse updateProfile(Long userId, PatientProfileRequest req) {
        PatientProfile profile = patientProfileRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found"));

        profile.setFullName(req.getFullName());
        profile.setPhone(req.getPhone());
        profile.setGender(req.getGender());
        profile.setDateOfBirth(req.getDateOfBirth());
        profile.setBloodGroup(req.getBloodGroup());
        profile.setAddress(req.getAddress());
        profile.setAllergies(req.getAllergies());
        profile.setEmergencyContact(req.getEmergencyContact());

        return toResponse(patientProfileRepository.save(profile));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PatientProfileResponse> getAllPatients(Pageable pageable) {
        return patientProfileRepository.findAll(pageable).map(this::toResponse);
    }

    @Override
    public void toggleStatus(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        user.setStatus(user.getStatus() == AccountStatus.ACTIVE
            ? AccountStatus.INACTIVE : AccountStatus.ACTIVE);
        userRepository.save(user);
    }

    // ── Mapping ───────────────────────────────────────────────

    private PatientProfileResponse toResponse(PatientProfile p) {
        PatientProfileResponse r = new PatientProfileResponse();
        r.setId(p.getId());
        r.setUserId(p.getUser().getId());
        r.setEmail(p.getUser().getEmail());
        r.setFullName(p.getFullName());
        r.setPhone(p.getPhone());
        r.setGender(p.getGender());
        r.setDateOfBirth(p.getDateOfBirth());
        r.setBloodGroup(p.getBloodGroup());
        r.setAddress(p.getAddress());
        r.setAllergies(p.getAllergies());
        r.setEmergencyContact(p.getEmergencyContact());
        r.setAccountStatus(p.getUser().getStatus().name());
        return r;
    }
}
