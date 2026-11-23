package com.hospital.entity;

import com.hospital.enums.AppointmentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Core appointment entity.
 *
 * Unique constraint: (doctor_id, appointment_date, start_time)
 * enforces the no-double-booking business rule at the DB level.
 */
@Entity
@Table(
    name = "appointment",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_appt_doctor_date_time",
        columnNames = {"doctor_id", "appointment_date", "start_time"}
    ),
    indexes = {
        @Index(name = "idx_appt_patient",  columnList = "patient_id"),
        @Index(name = "idx_appt_doctor",   columnList = "doctor_id"),
        @Index(name = "idx_appt_date",     columnList = "appointment_date"),
        @Index(name = "idx_appt_status",   columnList = "status")
    }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private PatientProfile patient;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "doctor_id", nullable = false)
    private DoctorProfile doctor;

    @Column(name = "appointment_date", nullable = false)
    private LocalDate appointmentDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private AppointmentStatus status = AppointmentStatus.PENDING;

    @Column(length = 500)
    private String reason;

    @Column(name = "cancel_reason", length = 500)
    private String cancelReason;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
