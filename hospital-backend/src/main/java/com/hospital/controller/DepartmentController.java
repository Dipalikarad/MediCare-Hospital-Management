package com.hospital.controller;

import com.hospital.dto.response.ApiResponse;
import com.hospital.entity.Department;
import com.hospital.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Public department listing (no auth required).
 * Used by the patient browse-doctors screen.
 */
@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<Department>>> getAll(
        @RequestParam(defaultValue = "0")  int page,
        @RequestParam(defaultValue = "50") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success(
            "Departments",
            departmentService.getAll(PageRequest.of(page, size, Sort.by("name")))
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Department>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Department", departmentService.getById(id)));
    }
}
