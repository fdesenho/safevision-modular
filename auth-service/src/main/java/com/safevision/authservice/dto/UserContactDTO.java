package com.safevision.authservice.dto;

/**
 * Data Transfer Object (DTO) representing public User Contact Information.
 * <p>
 * This record is designed to be exposed via API endpoints (e.g., {@code /auth/contact/{username}})
 * to allow other microservices (like the <b>Alert Service</b>) to fetch contact details
 * required for sending notifications (SMS/Email).
 * </p>
 * <p>
 * It intentionally excludes sensitive data like passwords or internal audit logs.
 * </p>
 *
 * @param id          The unique UUID of the user.
 * @param username    The login username.
 * @param phoneNumber The mobile number formatted for SMS providers (e.g., Twilio E.164 format).
 * @param email       The email address for correspondence.
 */
public record UserContactDTO(
    String id,
    String username,
    String phoneNumber,
    String email
) {}