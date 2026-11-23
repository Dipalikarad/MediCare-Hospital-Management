package com.hospital.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

/** Admin or Doctor creates/updates a doctor profile. */
@Data
public class DoctorProfileRequest {

    @NotBlank @Size(max = 150)
    private String fullName;

    @NotBlank @Size(max = 50)
    private String employeeId;

    @Pattern(regexp = "^[0-9+\\-() ]{7,15}$", message = "Invalid phone number")
    private String phone;

    @Size(max = 300)
    private String qualification;

    @Min(0) @Max(60)
    private Integer experienceYears;

    @DecimalMin("0.0") @Digits(integer = 8, fraction = 2)
    private BigDecimal consultationFee;

    @NotNull(message = "Department is required")
    private Long departmentId;
}
