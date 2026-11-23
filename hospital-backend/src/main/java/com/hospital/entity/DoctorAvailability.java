package com.hospital.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

/**
 * Represents a doctor's weekly availability for a specific day of week.
 * Business rule: appointments can only be booked within these windows.
 * DayOfWeek stored as INTEGER (1=Monday … 7=Sunday per ISO).
 */
@Entity
@Table(
    name = "doctor_availability",
    indexes = @Index(name = "idx_avail_doctor", columnList = "doctor_id")
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DoctorAvailability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "doctor_id", nullable = false)
    private DoctorProfile doctor;

    /**
     * ISO day-of-week: 1 = Monday, 7 = Sunday.
     * Business default: Sat (6) & Sun (7) are holidays unless a record exists.
     */
    @Column(name = "day_of_week", nullable = false)
    private Integer dayOfWeek;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    /** Duration of each bookable slot in minutes (e.g., 15, 20, 30). */
    @Column(name = "slot_duration_minutes", nullable = false)
    private Integer slotDurationMinutes;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;
}
