package com.safevision.authservice.dto;

import java.util.Set;

/**
 * Data Transfer Object (DTO) representing the safe public view of a User.
 * <p>
 * This record is used to return user details to the frontend or other services
 * after registration or updates, strictly excluding sensitive data like passwords.
 * </p>
 *
 * @param id       The unique UUID of the user.
 * @param username The login username.
 * @param roles    The security roles assigned to the user (e.g., ADMIN, USER).
 */
public record UserResponse(
    String id,
    String username,
    Set<String> roles
) {

    /**
     * Compact Constructor for validation and defensive copying.
     * Ensures the DTO is never created with null core fields and that the roles set is immutable.
     */
    public UserResponse {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        
        // Defensive copy: Ensures the Set inside the record cannot be modified externally later.
        // Also handles null by defaulting to an empty set.
        roles = (roles != null) ? Set.copyOf(roles) : Set.of();
    }
}