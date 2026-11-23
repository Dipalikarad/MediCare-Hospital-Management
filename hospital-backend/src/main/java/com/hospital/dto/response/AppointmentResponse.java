package com.hospital.dto.response;

import com.hospital.enums.AppointmentStatus;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/** Appointment summary DTO. */
@Data
public class AppointmentResponse {
    private Long              id;
    private Long              patientId;
    private String            patientName;
    private Long              doctorId;
    private String            doctorName;
    private String            departmentName;
    private LocalDate         appointmentDate;
    private LocalTime         startTime;
    private LocalTime         endTime;
    private AppointmentStatus status;
    private String            reason;
    private String            cancelReason;
    private LocalDateTime     createdAt;
}
