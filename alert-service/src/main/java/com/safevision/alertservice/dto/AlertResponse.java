package com.safevision.alertservice.dto;

import java.time.LocalDateTime;

public record AlertResponse(
    String id,
    String alertType,
    String description,
    String severity,
    String cameraId,
    boolean acknowledged,
    LocalDateTime createdAt
) {}