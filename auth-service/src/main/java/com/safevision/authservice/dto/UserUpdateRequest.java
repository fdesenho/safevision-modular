package com.safevision.authservice.dto;

/**
 * Data Transfer Object (DTO) for User Profile Updates.
 * <p>
 * This record encapsulates the fields that an authenticated user is allowed to modify.
 * All fields are optional; if a field is {@code null}, the service will retain the existing value.
 * </p>
 *
 * @param email               The new contact email (must be unique if changed).
 * @param phoneNumber         The new mobile number for SMS alerts (e.g., +55...).
 * @param cameraConnectionUrl The new RTSP/HTTP stream URL for the Vision Agent.
 * @param password            The new raw password (will be encrypted before saving).
 */
public record UserUpdateRequest(
    String email,
    String phoneNumber,
    String cameraConnectionUrl,
    String password 
) {}