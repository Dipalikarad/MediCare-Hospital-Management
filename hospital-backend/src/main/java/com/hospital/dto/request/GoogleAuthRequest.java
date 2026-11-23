package com.hospital.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** Payload for POST /api/auth/google (front-end sends its id_token). */
@Data
public class GoogleAuthRequest {

    @NotBlank(message = "Google ID token is required")
    private String idToken;
}
