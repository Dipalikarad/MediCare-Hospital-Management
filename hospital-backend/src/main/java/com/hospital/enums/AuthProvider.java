package com.hospital.enums;

/**
 * Authentication provider for a user account.
 * LOCAL  = email + BCrypt password registration.
 * GOOGLE = created via Google OAuth2 login.
 */
public enum AuthProvider {
    LOCAL,
    GOOGLE
}
