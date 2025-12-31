package com.safevision.authservice.dto;

import com.safevision.common.enums.AlertType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Detailed user profile including camera and notification settings")
public record UserProfileDTO(
    String id,
    String username,
    String email,
    String phoneNumber,
    String cameraConnectionUrl,
    @Schema(description = "Active notification channels for this user")
    List<AlertType> alertPreferences
) {}