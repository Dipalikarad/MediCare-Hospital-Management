package com.hospital.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Prescription created by a DOCTOR for a COMPLETED appointment.
 * Business rules:
 *  - Only one prescription per appointment.
 *  - Only the assigned doctor can create it.
 *  - Appointment must be COMPLETED.
 */
@Entity
@Table(
    name = "prescription",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_prescription_appointment",
        columnNames = "appointment_id"
    )
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Prescription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** One-to-one with Appointment (enforced by unique constraint). */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "appointment_id", nullable = false)
    private Appointment appointment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "doctor_id", nullable = false)
    private DoctorProfile doctor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private PatientProfile patient;

    @Column(length = 1000)
    private String diagnosis;

    @Column(length = 1000)
    private String advice;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /** Cascade ALL + orphanRemoval: items are owned by prescription. */
    @OneToMany(
        mappedBy = "prescription",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @Builder.Default
    private List<PrescriptionItem> items = new ArrayList<>();

    // ── Helper methods ───────────────────────────────────────
    public void addItem(PrescriptionItem item) {
        items.add(item);
        item.setPrescription(this);
    }

    public void removeItem(PrescriptionItem item) {
        items.remove(item);
        item.setPrescription(null);
    }
}
