package com.hospital.dto.response;

import com.hospital.enums.AccountStatus;
import com.hospital.enums.AuthProvider;
import com.hospital.enums.Role;
import lombok.Data;

import java.time.LocalDateTime;

/** Safe user summary – password is never included. */
@Data
public class UserResponse {
    private Long          id;
    private String        email;
    private Role          role;
    private AccountStatus status;
    private AuthProvider  authProvider;
    private LocalDateTime createdAt;
}
