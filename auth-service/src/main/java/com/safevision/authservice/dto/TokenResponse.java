package com.safevision.authservice.dto;

/**
 * Data Transfer Object (DTO) for the Authentication Token.
 * <p>
 * This record encapsulates the JWT (JSON Web Token) returned to the client
 * upon successful authentication (login). It serves as the standard response
 * contract for the {@code /auth/login} endpoint.
 * </p>
 *
 * @param token The signed JWT string containing user claims and expiration.
 */
public record TokenResponse(String token) {

    /**
     * Compact Constructor for validation.
     * Ensures the DTO is never created with an invalid token.
     */
    public TokenResponse {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Token cannot be null or empty");
        }
    }
}