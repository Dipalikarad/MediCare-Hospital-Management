package com.hospital.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Medical department / specialization.
 * One department -> Many DoctorProfiles.
 */
@Entity
@Table(
    name = "department",
    uniqueConstraints = @UniqueConstraint(name = "uk_dept_name", columnNames = "name")
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;
}
