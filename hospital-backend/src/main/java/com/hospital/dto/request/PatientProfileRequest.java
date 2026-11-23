package com.hospital.dto.request;

import com.hospital.enums.Gender;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PatientProfileRequest {

    @NotBlank @Size(max = 150)
    private String fullName;

    @Pattern(regexp = "^[0-9+\\-() ]{7,15}$", message = "Invalid phone number")
    private String phone;

    private Gender gender;

    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @Size(max = 5)
    private String bloodGroup;

    @Size(max = 500)
    private String address;

    @Size(max = 500)
    private String allergies;

    @Pattern(regexp = "^[0-9+\\-() ]{7,15}$", message = "Invalid emergency contact")
    private String emergencyContact;
}
