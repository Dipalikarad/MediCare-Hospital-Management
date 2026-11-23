package com.hospital.dto.response;

import lombok.Data;
import java.time.LocalDate;

/** Medical record DTO. */
@Data
public class MedicalRecordResponse {
    private Long      id;
    private Long      patientId;
    private String    patientName;
    private Long      doctorId;
    private String    doctorName;
    private Long      appointmentId;
    private String    diagnosis;
    private String    symptoms;
    private String    treatmentNotes;
    private LocalDate recordDate;
}
