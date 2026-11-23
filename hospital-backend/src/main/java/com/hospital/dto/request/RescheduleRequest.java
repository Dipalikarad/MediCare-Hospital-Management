package com.hospital.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

/** Patient reschedules an appointment to a new date/time. */
@Data
public class RescheduleRequest {

    @NotNull
    @FutureOrPresent(message = "Cannot reschedule to the past")
    private LocalDate newDate;

    @NotNull
    private LocalTime newStartTime;

    @Size(max = 500)
    private String reason;
}
