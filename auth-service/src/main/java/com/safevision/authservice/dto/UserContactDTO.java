package com.safevision.authservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Internal DTO for user contact mapping within the Alert Service")
public record UserContactDTO(
    @Schema(description = "Unique identifier of the user", example = "550e8400-e29b-41d4-a716-446655440000")
    String id,
    
    @Schema(description = "Username for identifying the notification recipient", example = "johndoe")
    String username,
    
    @Schema(description = "E.164 formatted phone number for SMS/Twilio", example = "+5548999999999")
    String phoneNumber,
    
    @Schema(description = "User's email for alert delivery", example = "john@safevision.com")
    String email
) {}