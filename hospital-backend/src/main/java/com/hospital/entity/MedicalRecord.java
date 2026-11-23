package com.hospital.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * Medical record for a patient, written by a doctor.
 * Optional link to an appointment (appointment_id may be null).
 * Business rule: a doctor can only create records for patients
 * who have had an appointment with them.
 */
@Entity
@Table(
    name = "medical_record",
    indexes = {
        @Index(name = "idx_mr_patient",     columnList = "patient_id"),
        @Index(name = "idx_mr_doctor",      columnList = "doctor_id"),
        @Index(name = "idx_mr_appointment", columnList = "appointment_id")
    }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MedicalRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private PatientProfile patient;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "doctor_id", nullable = false)
    private DoctorProfile doctor;

    /** Nullable — a record can exist without being tied to one appointment. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id")
    private Appointment appointment;

    @Column(length = 1000)
    private String diagnosis;

    @Column(length = 1000)
    private String symptoms;

    @Column(name = "treatment_notes", length = 2000)
    private String treatmentNotes;

    @Column(name = "record_date", nullable = false)
    private LocalDate recordDate;
}
