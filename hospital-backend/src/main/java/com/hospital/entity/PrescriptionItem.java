package com.hospital.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Individual medicine line in a Prescription.
 * Owned by Prescription via CASCADE ALL / orphanRemoval.
 */
@Entity
@Table(
    name = "prescription_item",
    indexes = @Index(name = "idx_pi_prescription", columnList = "prescription_id")
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PrescriptionItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "prescription_id", nullable = false)
    private Prescription prescription;

    @Column(name = "medicine_name", nullable = false, length = 200)
    private String medicineName;

    @Column(length = 100)
    private String dosage;

    @Column(length = 100)
    private String frequency;

    @Column(name = "duration_days")
    private Integer durationDays;

    @Column(length = 500)
    private String instructions;
}
