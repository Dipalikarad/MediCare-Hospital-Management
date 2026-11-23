package com.hospital.service;

import com.hospital.dto.request.*;
import com.hospital.dto.response.JwtAuthResponse;

/**
 * Authentication service interface.
 */
public interface AuthService {

    JwtAuthResponse register(RegisterRequest request);

    JwtAuthResponse login(LoginRequest request);

    /** Verify a Google ID token from the frontend and return a JWT. */
    JwtAuthResponse googleLogin(GoogleAuthRequest request);

    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);
}
