package com.safevision.authservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Authentication token response")
public record TokenResponse(
    @Schema(description = "Signed JWT token to be used in the Authorization header")
    String token
) {
    public TokenResponse {
        if (token == null || token.isBlank()) throw new IllegalArgumentException("Token cannot be null or empty");
    }
}