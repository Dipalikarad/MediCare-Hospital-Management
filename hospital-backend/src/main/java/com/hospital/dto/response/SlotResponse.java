package com.hospital.dto.response;

import lombok.Data;
import java.time.LocalTime;

/** Available booking slot for a doctor on a given date. */
@Data
public class SlotResponse {
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean   available;
}
