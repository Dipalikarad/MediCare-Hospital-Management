package com.hospital.controller;

import com.hospital.dto.request.*;
import com.hospital.dto.response.ApiResponse;
import com.hospital.dto.response.JwtAuthResponse;
import com.hospital.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Public authentication endpoints.
 * No JWT required.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /** POST /api/auth/register — patient self-registration */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<JwtAuthResponse>> register(
        @Valid @RequestBody RegisterRequest request
    ) {
        JwtAuthResponse resp = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Registration successful", resp));
    }

    /** POST /api/auth/login — email + password login */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtAuthResponse>> login(
        @Valid @RequestBody LoginRequest request
    ) {
        JwtAuthResponse resp = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", resp));
    }

    /** POST /api/auth/google — Google OAuth2 (frontend sends Google id_token) */
    @PostMapping("/google")
    public ResponseEntity<ApiResponse<JwtAuthResponse>> googleLogin(
        @Valid @RequestBody GoogleAuthRequest request
    ) {
        JwtAuthResponse resp = authService.googleLogin(request);
        return ResponseEntity.ok(ApiResponse.success("Google login successful", resp));
    }

    /** POST /api/auth/forgot-password */
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
        @Valid @RequestBody ForgotPasswordRequest request
    ) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(
            ApiResponse.success("If that email exists, a reset link has been sent.")
        );
    }

    /** POST /api/auth/reset-password */
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
        @Valid @RequestBody ResetPasswordRequest request
    ) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success("Password reset successfully"));
    }
}
