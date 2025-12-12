package com.safevision.authservice.dto;

import java.util.Set;

/**
 * Data Transfer Object (DTO) for User Registration.
 * <p>
 * Defines the contract for the incoming JSON payload when creating a new account.
 * It leverages Java Records for immutability and concise syntax.
 * </p>
 *
 * @param username    The unique identifier for login.
 * @param password    The raw password (will be encrypted by the service).
 * @param email       The contact email for notifications and recovery.
 * @param phoneNumber The mobile number formatted for SMS alerts (Twilio, e.g., +55...).
 * @param cameraUrl   The connection string (RTSP/HTTP) for the Vision Agent.
 * @param roles       The set of permissions (e.g., "ADMIN", "USER"). Can be null (defaults to USER).
 */


import com.safevision.common.enums.AlertType;

import java.util.Set;

/**
 * RegisterRequest record - immutable DTO for registration payload.
 */
public record RegisterRequest(
    String username,
    String password,
    String email,
    String phoneNumber,
    String cameraUrl,
    Set<String> roles,
    Set<AlertType> alertTypes   // <-- agora usamos o enum
) {}

