package com.hospital.dto.response;

import com.hospital.enums.Gender;
import lombok.Data;
import java.time.LocalDate;

/** Patient profile DTO returned to clients. */
@Data
public class PatientProfileResponse {
    private Long      id;
    private Long      userId;
    private String    email;
    private String    fullName;
    private String    phone;
    private Gender    gender;
    private LocalDate dateOfBirth;
    private String    bloodGroup;
    private String    address;
    private String    allergies;
    private String    emergencyContact;
    private String    accountStatus;
}
