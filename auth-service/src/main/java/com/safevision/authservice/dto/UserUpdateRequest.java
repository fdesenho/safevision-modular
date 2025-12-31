package com.safevision.authservice.dto;

import com.safevision.common.enums.AlertType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;

@Schema(description = "Request payload for updating user profile and preferences")
public record UserUpdateRequest(
    @Schema(example = "newemail@domain.com")
    String email,
    
    @Schema(example = "+554888888888")
    String phoneNumber,
    
    @Schema(description = "The RTSP/HTTP URL for the user's camera agent")
    String cameraConnectionUrl,
    
    @Schema(description = "New password (will be re-hashed if provided)")
    String password,
    
    @Schema(description = "Set of preferred notification channels")
    Set<AlertType> alertPreferences 
) {}