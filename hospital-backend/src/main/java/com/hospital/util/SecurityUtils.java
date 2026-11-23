package com.hospital.util;

import com.hospital.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Utility to extract the currently authenticated user's details
 * from the SecurityContext in service-layer ownership checks.
 */
@Component
@RequiredArgsConstructor
public class SecurityUtils {

    private final JwtTokenProvider jwtTokenProvider;

    /** Returns the email of the currently authenticated user. */
    public static String getCurrentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new com.hospital.exception.UnauthorizedException("Not authenticated");
        }
        return auth.getName();
    }

    /** Returns the granted role of the current user (e.g. ROLE_ADMIN). */
    public static String getCurrentUserRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;
        return auth.getAuthorities().stream()
            .findFirst()
            .map(Object::toString)
            .orElse(null);
    }
}
