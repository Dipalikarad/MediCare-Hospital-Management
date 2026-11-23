package com.hospital.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

/** Doctor creates a prescription for a completed appointment. */
@Data
public class PrescriptionRequest {

    @NotNull(message = "Appointment ID is required")
    private Long appointmentId;

    @Size(max = 1000)
    private String diagnosis;

    @Size(max = 1000)
    private String advice;

    @NotEmpty(message = "At least one prescription item is required")
    @Valid
    private List<ItemRequest> items;

    @Data
    public static class ItemRequest {

        @NotBlank(message = "Medicine name is required")
        @Size(max = 200)
        private String medicineName;

        @Size(max = 100)
        private String dosage;

        @Size(max = 100)
        private String frequency;

        @Min(1)
        private Integer durationDays;

        @Size(max = 500)
        private String instructions;
    }
}
