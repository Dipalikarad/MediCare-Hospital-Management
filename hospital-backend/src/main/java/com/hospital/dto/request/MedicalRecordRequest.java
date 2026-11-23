package com.hospital.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

/** Doctor adds a medical record for a patient. */
@Data
public class MedicalRecordRequest {

    @NotNull(message = "Patient ID is required")
    private Long patientId;

    /** Optional – links record to a specific appointment. */
    private Long appointmentId;

    @Size(max = 1000)
    private String diagnosis;

    @Size(max = 1000)
    private String symptoms;

    @Size(max = 2000)
    private String treatmentNotes;

    @NotNull(message = "Record date is required")
    private LocalDate recordDate;
}
