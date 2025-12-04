package com.safevision.authservice.dto;

/**
 * Data Transfer Object (DTO) for User Authentication.
 * <p>
 * This record encapsulates the credentials sent by the client (Frontend)
 * to the {@code /auth/login} endpoint.
 * </p>
 *
 * @param username The unique username identifying the user in the system.
 * @param password The raw password provided by the user (to be verified against the stored hash).
 */
public record LoginRequest(
    String username,
    String password
) {}