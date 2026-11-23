package com.hospital.repository;

import com.hospital.entity.DoctorProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DoctorProfileRepository extends JpaRepository<DoctorProfile, Long> {

    Optional<DoctorProfile> findByUserId(Long userId);

    boolean existsByEmployeeId(String employeeId);

    /** Search by name or department name, case-insensitive. */
    @Query("""
           SELECT d FROM DoctorProfile d
           JOIN d.department dept
           WHERE (:name IS NULL OR LOWER(d.fullName) LIKE LOWER(CONCAT('%', :name, '%')))
             AND (:departmentId IS NULL OR dept.id = :departmentId)
           """)
    Page<DoctorProfile> searchDoctors(
        @Param("name") String name,
        @Param("departmentId") Long departmentId,
        Pageable pageable
    );

    long countByDepartmentId(Long departmentId);
}
