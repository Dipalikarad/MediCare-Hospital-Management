package com.hospital.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** JWT token returned after a successful login / registration. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtAuthResponse {
    @Builder.Default
    private String tokenType = "Bearer";
    private String accessToken;
    private Long   userId;
    private String email;
    private String role;
}
