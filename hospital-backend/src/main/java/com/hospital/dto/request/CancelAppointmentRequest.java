package com.hospital.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

/** Patient cancels an appointment (optional reason). */
@Data
public class CancelAppointmentRequest {

    @Size(max = 500)
    private String cancelReason;
}
