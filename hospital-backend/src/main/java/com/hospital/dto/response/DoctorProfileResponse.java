package com.hospital.dto.response;

import lombok.Data;
import java.math.BigDecimal;

/** Doctor profile DTO returned to clients. */
@Data
public class DoctorProfileResponse {
    private Long       id;
    private Long       userId;
    private String     email;
    private String     fullName;
    private String     employeeId;
    private String     phone;
    private String     qualification;
    private Integer    experienceYears;
    private BigDecimal consultationFee;
    private Long       departmentId;
    private String     departmentName;
    private String     accountStatus;
}
