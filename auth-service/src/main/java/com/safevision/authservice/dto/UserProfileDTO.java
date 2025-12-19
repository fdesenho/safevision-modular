package com.safevision.authservice.dto;

import com.safevision.common.enums.AlertType;
import java.util.List;

public record UserProfileDTO(
    String id,
    String username,
    String email,
    String phoneNumber,
    String cameraConnectionUrl,
    List<AlertType> alertPreferences
) {}