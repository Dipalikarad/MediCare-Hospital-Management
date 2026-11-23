package com.hospital.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

/** Admin creates a new doctor account. */
@Data
public class CreateDoctorRequest {

    @NotBlank @jakarta.validation.constraints.Email
    private String email;

    @NotBlank @Size(min = 8)
    private String password;

    @NotBlank @Size(max = 150)
    private String fullName;

    @NotBlank @Size(max = 50)
    private String employeeId;

    @Pattern(regexp = "^[0-9+\\-() ]{7,15}$", message = "Invalid phone")
    private String phone;

    @Size(max = 300)
    private String qualification;

    @Min(0) @Max(60)
    private Integer experienceYears;

    @DecimalMin("0.0") @Digits(integer = 8, fraction = 2)
    private java.math.BigDecimal consultationFee;

    @NotNull
    private Long departmentId;
}
