package com.hospital.repository;

import com.hospital.entity.MedicalRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {

    Page<MedicalRecord> findByPatientId(Long patientId, Pageable pageable);

    Page<MedicalRecord> findByDoctorId(Long doctorId, Pageable pageable);

    Page<MedicalRecord> findByPatientIdAndDoctorId(
        Long patientId, Long doctorId, Pageable pageable
    );
}
