package com.hospital.service;

import com.hospital.dto.request.DepartmentRequest;
import com.hospital.entity.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DepartmentService {

    Department create(DepartmentRequest request);

    Department update(Long id, DepartmentRequest request);

    void delete(Long id);

    Department getById(Long id);

    Page<Department> getAll(Pageable pageable);
}
