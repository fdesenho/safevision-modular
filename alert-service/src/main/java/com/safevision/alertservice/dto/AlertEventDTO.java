package com.safevision.alertservice.dto;

public record AlertEventDTO(
    String userId,
    String alertType,
    String description,
    String severity,
    String cameraId
) {}