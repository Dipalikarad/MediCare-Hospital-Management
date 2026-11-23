package com.hospital.entity;

import com.hospital.enums.Gender;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * Patient-specific profile linked 1:1 to a User.
 */
@Entity
@Table(
    name = "patient_profile",
    uniqueConstraints = @UniqueConstraint(name = "uk_patient_user", columnNames = "user_id")
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PatientProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(length = 15)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Gender gender;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "blood_group", length = 5)
    private String bloodGroup;

    @Column(length = 500)
    private String address;

    @Column(length = 500)
    private String allergies;

    @Column(name = "emergency_contact", length = 15)
    private String emergencyContact;
}
