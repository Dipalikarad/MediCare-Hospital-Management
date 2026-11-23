package com.hospital.service.impl;

import com.hospital.dto.request.*;
import com.hospital.dto.response.JwtAuthResponse;
import com.hospital.entity.*;
import com.hospital.enums.*;
import com.hospital.exception.*;
import com.hospital.repository.*;
import com.hospital.security.JwtTokenProvider;
import com.hospital.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Handles: register, login, Google OAuth2, forgot/reset password.
 *
 * Google token verification uses Google's tokeninfo endpoint.
 * Production note: use google-api-client library for full verification.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository              userRepository;
    private final PatientProfileRepository    patientProfileRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder             passwordEncoder;
    private final AuthenticationManager      authenticationManager;
    private final JwtTokenProvider           jwtTokenProvider;
    private final JavaMailSender             mailSender;

    @Value("${app.password-reset.expiry-minutes:30}")
    private int resetExpiryMinutes;

    @Value("${app.google.client-id:}")
    private String googleClientId;

    // ── Register ─────────────────────────────────────────────

    @Override
    public JwtAuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new BusinessException("Email already registered: " + req.getEmail());
        }

        User user = User.builder()
            .email(req.getEmail())
            .password(passwordEncoder.encode(req.getPassword()))
            .role(Role.PATIENT)
            .status(AccountStatus.ACTIVE)
            .authProvider(AuthProvider.LOCAL)
            .build();
        userRepository.save(user);

        // Create empty patient profile
        PatientProfile profile = PatientProfile.builder()
            .user(user)
            .fullName(req.getFullName())
            .build();
        patientProfileRepository.save(profile);

        log.info("New patient registered: {}", user.getEmail());
        return buildJwtResponse(user);
    }

    // ── Login ─────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public JwtAuthResponse login(LoginRequest req) {
        Authentication auth = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword())
        );

        User user = userRepository.findByEmail(req.getEmail())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return buildJwtResponse(user);
    }

    // ── Google OAuth2 ─────────────────────────────────────────

    @Override
    public JwtAuthResponse googleLogin(GoogleAuthRequest req) {
        // Verify the Google ID token via Google's tokeninfo endpoint
        GoogleTokenInfo info = verifyGoogleToken(req.getIdToken());

        User user = userRepository.findByEmail(info.email()).orElseGet(() -> {
            // First-time Google login → create PATIENT account
            User newUser = User.builder()
                .email(info.email())
                .role(Role.PATIENT)
                .status(AccountStatus.ACTIVE)
                .authProvider(AuthProvider.GOOGLE)
                .build();
            userRepository.save(newUser);

            PatientProfile profile = PatientProfile.builder()
                .user(newUser)
                .fullName(info.name())
                .build();
            patientProfileRepository.save(profile);

            log.info("New Google patient created: {}", newUser.getEmail());
            return newUser;
        });

        if (user.getStatus() == AccountStatus.INACTIVE) {
            throw new BusinessException("Your account has been deactivated.");
        }

        return buildJwtResponse(user);
    }

    // ── Forgot / Reset password ───────────────────────────────

    @Override
    public void forgotPassword(ForgotPasswordRequest req) {
        userRepository.findByEmail(req.getEmail()).ifPresent(user -> {
            // Invalidate old tokens for this user
            passwordResetTokenRepository.deleteByUserId(user.getId());

            String token = UUID.randomUUID().toString();
            PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusMinutes(resetExpiryMinutes))
                .build();
            passwordResetTokenRepository.save(resetToken);

            sendResetEmail(user.getEmail(), token);
            log.info("Password reset email sent to: {}", user.getEmail());
        });
        // Always return 200 to prevent user enumeration
    }

    @Override
    public void resetPassword(ResetPasswordRequest req) {
        PasswordResetToken resetToken = passwordResetTokenRepository
            .findByToken(req.getToken())
            .orElseThrow(() -> new BusinessException("Invalid or expired reset token"));

        if (resetToken.isExpired()) {
            passwordResetTokenRepository.delete(resetToken);
            throw new BusinessException("Reset token has expired. Please request a new one.");
        }

        User user = resetToken.getUser();
        if (user.getAuthProvider() == AuthProvider.GOOGLE) {
            throw new BusinessException("Google accounts cannot reset password here.");
        }

        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);

        // Clean up all tokens for this user
        passwordResetTokenRepository.deleteByUserId(user.getId());
        log.info("Password reset for user: {}", user.getEmail());
    }

    // ── Private helpers ───────────────────────────────────────

    private JwtAuthResponse buildJwtResponse(User user) {
        String token = jwtTokenProvider.generateToken(user);
        return JwtAuthResponse.builder()
            .accessToken(token)
            .tokenType("Bearer")
            .userId(user.getId())
            .email(user.getEmail())
            .role(user.getRole().name())
            .build();
    }

    private void sendResetEmail(String toEmail, String token) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(toEmail);
        mail.setSubject("Hospital Management – Password Reset");
        mail.setText(
            "Click the link below to reset your password (valid for " + resetExpiryMinutes + " minutes):\n\n" +
            "http://localhost:5173/reset-password?token=" + token + "\n\n" +
            "If you did not request this, ignore this email."
        );
        try {
            mailSender.send(mail);
        } catch (Exception e) {
            log.error("Failed to send password reset email: {}", e.getMessage());
        }
    }

    /**
     * Verify Google ID token via Google's tokeninfo API.
     * Production: replace with google-auth-library or JWT signature verification.
     */
    private GoogleTokenInfo verifyGoogleToken(String idToken) {
        if (googleClientId == null || googleClientId.isBlank()) {
            throw new BusinessException("Google login is not configured. Set GOOGLE_CLIENT_ID.");
        }
        try {
            String url = "https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken;
            org.springframework.web.client.RestTemplate rt = new org.springframework.web.client.RestTemplate();
            @SuppressWarnings("unchecked")
            java.util.Map<String, String> response = rt.getForObject(url, java.util.Map.class);
            if (response == null) throw new BusinessException("Could not verify Google token");
            if (!googleClientId.equals(response.get("aud"))) {
                throw new BusinessException("Google token was issued for a different OAuth client.");
            }
            if (!Boolean.parseBoolean(response.get("email_verified"))) {
                throw new BusinessException("Google account email is not verified.");
            }
            return new GoogleTokenInfo(
                response.get("email"),
                response.getOrDefault("name", response.get("email"))
            );
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Google token verification failed.");
            throw new BusinessException("Google login failed. Please try again.");
        }
    }

    private record GoogleTokenInfo(String email, String name) {}
}
