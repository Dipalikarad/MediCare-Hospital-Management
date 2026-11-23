package com.hospital.enums;

/**
 * Full lifecycle of an appointment.
 * State transitions:
 *   PENDING → CONFIRMED → COMPLETED
 *   PENDING / CONFIRMED → CANCELLED (by patient, within allowed window)
 *   CONFIRMED → NO_SHOW (by doctor)
 */
public enum AppointmentStatus {
    PENDING,
    CONFIRMED,
    COMPLETED,
    CANCELLED,
    NO_SHOW
}
