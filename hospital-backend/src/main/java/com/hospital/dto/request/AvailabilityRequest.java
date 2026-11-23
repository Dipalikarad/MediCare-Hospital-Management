package com.hospital.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalTime;

/** Create or update a doctor availability entry for one day of week. */
@Data
public class AvailabilityRequest {

    /** ISO day of week: 1=Monday … 7=Sunday */
    @NotNull @Min(1) @Max(7)
    private Integer dayOfWeek;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;

    @NotNull @Min(5) @Max(120)
    private Integer slotDurationMinutes;

    private Boolean active = true;
}
