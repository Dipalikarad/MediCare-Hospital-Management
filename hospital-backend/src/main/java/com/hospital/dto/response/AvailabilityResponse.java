package com.hospital.dto.response;

import lombok.Data;
import java.time.LocalTime;

/** Doctor weekly availability entry. */
@Data
public class AvailabilityResponse {
    private Long      id;
    private Integer   dayOfWeek;
    private String    dayName;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer   slotDurationMinutes;
    private Boolean   active;
}
