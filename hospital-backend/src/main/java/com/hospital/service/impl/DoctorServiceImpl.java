package com.hospital.service.impl;

import com.hospital.dto.request.CreateDoctorRequest;
import com.hospital.dto.request.DoctorProfileRequest;
import com.hospital.dto.response.DoctorProfileResponse;
import com.hospital.entity.*;
import com.hospital.enums.*;
import com.hospital.exception.*;
import com.hospital.repository.*;
import com.hospital.service.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class DoctorServiceImpl implements DoctorService {

    private final UserRepository          userRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final DepartmentRepository    departmentRepository;
    private final PasswordEncoder         passwordEncoder;

    @Override
    public DoctorProfileResponse createDoctor(CreateDoctorRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new BusinessException("Email already in use: " + req.getEmail());
        }
        if (doctorProfileRepository.existsByEmployeeId(req.getEmployeeId())) {
            throw new BusinessException("Employee ID already in use: " + req.getEmployeeId());
        }

        Department dept = departmentRepository.findById(req.getDepartmentId())
            .orElseThrow(() -> new ResourceNotFoundException("Department not found"));

        User user = User.builder()
            .email(req.getEmail())
            .password(passwordEncoder.encode(req.getPassword()))
            .role(Role.DOCTOR)
            .status(AccountStatus.ACTIVE)
            .authProvider(AuthProvider.LOCAL)
            .build();
        userRepository.save(user);

        DoctorProfile profile = DoctorProfile.builder()
            .user(user)
            .fullName(req.getFullName())
            .employeeId(req.getEmployeeId())
            .phone(req.getPhone())
            .qualification(req.getQualification())
            .experienceYears(req.getExperienceYears())
            .consultationFee(req.getConsultationFee())
            .department(dept)
            .build();
        doctorProfileRepository.save(profile);

        return toResponse(profile);
    }

    @Override
    public DoctorProfileResponse updateProfile(Long doctorProfileId, DoctorProfileRequest req) {
        DoctorProfile profile = doctorProfileRepository.findById(doctorProfileId)
            .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found"));
        Department dept = departmentRepository.findById(req.getDepartmentId())
            .orElseThrow(() -> new ResourceNotFoundException("Department not found"));

        profile.setFullName(req.getFullName());
        profile.setPhone(req.getPhone());
        profile.setQualification(req.getQualification());
        profile.setExperienceYears(req.getExperienceYears());
        profile.setConsultationFee(req.getConsultationFee());
        profile.setDepartment(dept);
        doctorProfileRepository.save(profile);

        return toResponse(profile);
    }

    @Override
    @Transactional(readOnly = true)
    public DoctorProfileResponse getByUserId(Long userId) {
        return doctorProfileRepository.findByUserId(userId)
            .map(this::toResponse)
            .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found for user " + userId));
    }

    @Override
    @Transactional(readOnly = true)
    public DoctorProfileResponse getById(Long id) {
        return doctorProfileRepository.findById(id)
            .map(this::toResponse)
            .orElseThrow(() -> new ResourceNotFoundException("Doctor not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DoctorProfileResponse> searchDoctors(String name, Long deptId, Pageable pageable) {
        return doctorProfileRepository
            .searchDoctors(name, deptId, pageable)
            .map(this::toResponse);
    }

    @Override
    public void toggleStatus(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        user.setStatus(user.getStatus() == AccountStatus.ACTIVE
            ? AccountStatus.INACTIVE : AccountStatus.ACTIVE);
        userRepository.save(user);
    }

    // ── Mapping helper ────────────────────────────────────────

    private DoctorProfileResponse toResponse(DoctorProfile p) {
        DoctorProfileResponse r = new DoctorProfileResponse();
        r.setId(p.getId());
        r.setUserId(p.getUser().getId());
        r.setEmail(p.getUser().getEmail());
        r.setFullName(p.getFullName());
        r.setEmployeeId(p.getEmployeeId());
        r.setPhone(p.getPhone());
        r.setQualification(p.getQualification());
        r.setExperienceYears(p.getExperienceYears());
        r.setConsultationFee(p.getConsultationFee());
        r.setDepartmentId(p.getDepartment().getId());
        r.setDepartmentName(p.getDepartment().getName());
        r.setAccountStatus(p.getUser().getStatus().name());
        return r;
    }
}
