package com.hospital.repository;

import com.hospital.entity.Appointment;
import com.hospital.enums.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // ── Doctor double-booking check ──────────────────────────
    boolean existsByDoctorIdAndAppointmentDateAndStartTime(
        Long doctorId, LocalDate date, LocalTime startTime
    );

    // ── Patient overlap check ─────────────────────────────────
    @Query("""
           SELECT COUNT(a) > 0 FROM Appointment a
           WHERE a.patient.id = :patientId
             AND a.appointmentDate = :date
             AND a.status NOT IN ('CANCELLED', 'NO_SHOW')
             AND a.startTime < :endTime
             AND a.endTime   > :startTime
           """)
    boolean hasPatientOverlap(
        @Param("patientId") Long patientId,
        @Param("date")      LocalDate date,
        @Param("startTime") LocalTime startTime,
        @Param("endTime")   LocalTime endTime
    );

    // ── Patient-facing queries ────────────────────────────────
    Page<Appointment> findByPatientId(Long patientId, Pageable pageable);

    Page<Appointment> findByPatientIdAndStatus(
        Long patientId, AppointmentStatus status, Pageable pageable
    );

    // ── Doctor-facing queries ─────────────────────────────────
    Page<Appointment> findByDoctorId(Long doctorId, Pageable pageable);

    List<Appointment> findByDoctorIdAndAppointmentDateAndStatus(
        Long doctorId, LocalDate date, AppointmentStatus status
    );

    // ── Admin reports ─────────────────────────────────────────
    @Query("""
           SELECT a FROM Appointment a
           WHERE (:doctorId IS NULL OR a.doctor.id = :doctorId)
             AND (:status   IS NULL OR a.status    = :status)
             AND (:from     IS NULL OR a.appointmentDate >= :from)
             AND (:to       IS NULL OR a.appointmentDate <= :to)
           """)
    Page<Appointment> findAllFiltered(
        @Param("doctorId") Long doctorId,
        @Param("status")   AppointmentStatus status,
        @Param("from")     LocalDate from,
        @Param("to")       LocalDate to,
        Pageable pageable
    );

    long countByStatus(AppointmentStatus status);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.appointmentDate = :date")
    long countByDate(@Param("date") LocalDate date);

    /** Today's appointments for a specific doctor. */
    List<Appointment> findByDoctorIdAndAppointmentDate(Long doctorId, LocalDate date);

    /** Count appointments per doctor (for reports). */
    @Query("""
           SELECT a.doctor.id, COUNT(a)
           FROM Appointment a
           WHERE a.appointmentDate BETWEEN :from AND :to
           GROUP BY a.doctor.id
           """)
    List<Object[]> countPerDoctor(@Param("from") LocalDate from, @Param("to") LocalDate to);

    /** Count appointments per department. */
    @Query("""
           SELECT a.doctor.department.id, COUNT(a)
           FROM Appointment a
           WHERE a.appointmentDate BETWEEN :from AND :to
           GROUP BY a.doctor.department.id
           """)
    List<Object[]> countPerDepartment(
        @Param("from") LocalDate from, @Param("to") LocalDate to
    );

    /** Daily appointment counts in a date range. */
    @Query("""
           SELECT a.appointmentDate, COUNT(a)
           FROM Appointment a
           WHERE a.appointmentDate BETWEEN :from AND :to
           GROUP BY a.appointmentDate
           ORDER BY a.appointmentDate
           """)
    List<Object[]> dailyCounts(@Param("from") LocalDate from, @Param("to") LocalDate to);

    /** Check if a patient has ever had an appointment with a doctor. */
    @Query("""
           SELECT COUNT(a) > 0 FROM Appointment a
           WHERE a.patient.id = :patientId
             AND a.doctor.id  = :doctorId
           """)
    boolean hasPatientAppointmentWithDoctor(
        @Param("patientId") Long patientId,
        @Param("doctorId")  Long doctorId
    );

    Optional<Appointment> findByIdAndDoctorId(Long id, Long doctorId);

    Optional<Appointment> findByIdAndPatientId(Long id, Long patientId);
}
