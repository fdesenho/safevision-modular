package com.safevision.authservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;

@Schema(description = "Public user information response")
public record UserResponse(
    String id,
    String username,
    @Schema(description = "Assigned security roles")
    Set<String> roles
) {
    public UserResponse {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("User ID cannot be null or empty");
        if (username == null || username.isBlank()) throw new IllegalArgumentException("Username cannot be null or empty");
        roles = (roles != null) ? Set.copyOf(roles) : Set.of();
    }
}