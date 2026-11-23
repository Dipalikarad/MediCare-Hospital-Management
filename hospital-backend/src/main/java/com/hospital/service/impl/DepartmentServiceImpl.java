package com.hospital.service.impl;

import com.hospital.dto.request.DepartmentRequest;
import com.hospital.entity.Department;
import com.hospital.exception.BusinessException;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.repository.DepartmentRepository;
import com.hospital.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;

    @Override
    public Department create(DepartmentRequest req) {
        if (departmentRepository.existsByNameIgnoreCase(req.getName())) {
            throw new BusinessException("Department already exists: " + req.getName());
        }
        Department dept = Department.builder()
            .name(req.getName())
            .description(req.getDescription())
            .build();
        return departmentRepository.save(dept);
    }

    @Override
    public Department update(Long id, DepartmentRequest req) {
        Department dept = getById(id);
        departmentRepository.findByNameIgnoreCase(req.getName())
            .filter(d -> !d.getId().equals(id))
            .ifPresent(d -> { throw new BusinessException("Department name already in use"); });
        dept.setName(req.getName());
        dept.setDescription(req.getDescription());
        return departmentRepository.save(dept);
    }

    @Override
    public void delete(Long id) {
        Department dept = getById(id);
        departmentRepository.delete(dept);
    }

    @Override
    @Transactional(readOnly = true)
    public Department getById(Long id) {
        return departmentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Department> getAll(Pageable pageable) {
        return departmentRepository.findAll(pageable);
    }
}
