package com.hospital.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Doctor-specific profile linked 1:1 to a User.
 * Belongs to one Department (Many doctors per department).
 */
@Entity
@Table(
    name = "doctor_profile",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_doctor_user", columnNames = "user_id"),
        @UniqueConstraint(name = "uk_doctor_emp_id", columnNames = "employee_id")
    },
    indexes = @Index(name = "idx_doctor_dept", columnList = "department_id")
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DoctorProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Owning side of the 1:1 relationship. */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(name = "employee_id", nullable = false, length = 50)
    private String employeeId;

    @Column(length = 15)
    private String phone;

    @Column(length = 300)
    private String qualification;

    @Column(name = "experience_years")
    private Integer experienceYears;

    @Column(name = "consultation_fee", precision = 10, scale = 2)
    private java.math.BigDecimal consultationFee;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;
}
