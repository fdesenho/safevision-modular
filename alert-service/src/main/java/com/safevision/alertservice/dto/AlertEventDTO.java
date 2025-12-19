package com.safevision.alertservice.dto;

import java.math.BigDecimal;

public record AlertEventDTO(
    
    String userId,
    String alertType,
    String description,
    String severity,
    String cameraId,
    String snapshotUrl,
    BigDecimal latitude,
    BigDecimal longitude,
    String address // 📍 Novo campo
) {}