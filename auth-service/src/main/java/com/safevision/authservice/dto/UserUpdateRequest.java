package com.safevision.authservice.dto;

import com.safevision.common.enums.AlertType;
import java.util.Set;


public record UserUpdateRequest(
    String email,
    String phoneNumber,
    String cameraConnectionUrl,
    String password,
    Set<AlertType> alertPreferences 
) {}