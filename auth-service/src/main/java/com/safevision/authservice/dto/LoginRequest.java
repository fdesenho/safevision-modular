package com.safevision.authservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "User credentials for authentication")
public record LoginRequest(
    @Schema(required = true, example = "admin")
    String username,
    
    @Schema(required = true, example = "secret123")
    String password
) {}