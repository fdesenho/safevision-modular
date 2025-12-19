package com.safevision.alertservice.dto;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AlertResponse(
    String id,
    String alertType,
    String description,
    String severity,
    String cameraId,
    boolean acknowledged,
    LocalDateTime createdAt,
    String snapshotUrl,
    BigDecimal latitude,
    BigDecimal longitude,
    String address // 📍 Adicionado
) {}