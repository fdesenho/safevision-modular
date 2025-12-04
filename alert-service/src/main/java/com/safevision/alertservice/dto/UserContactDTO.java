package com.safevision.alertservice.dto;

/**
 * Data Transfer Object (DTO) representing User Contact Information.
 * <p>
 * This record is used to transport user details (specifically contact info like phone and email)
 * fetched from the **Auth Service** to be used by the **Alert Service** for notifications (Twilio/Email).
 * </p>
 *
 * @param id          The unique UUID of the user.
 * @param username    The user's login username.
 * @param phoneNumber The user's mobile number (formatted for Twilio, e.g., +55...).
 * @param email       The user's email address for notifications.
 */
public record UserContactDTO(
    String id,
    String username,
    String phoneNumber,
    String email
) {}