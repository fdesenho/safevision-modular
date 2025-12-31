package com.safevision.alertservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "Payload for creating a new security alert event")
public record AlertEventDTO(
    @Schema(description = "UUID of the user associated with the device", required = true)
    String userId,
    
    @Schema(description = "Classification of the security event", example = "INTRUSION_DETECTED")
    String alertType,
    
    @Schema(description = "Textual summary of the event")
    String description,
    
    @Schema(description = "Severity level", example = "WARNING")
    String severity,
    
    @Schema(description = "Source hardware ID")
    String cameraId,
    
    @Schema(description = "Cloud storage link for the captured frame")
    String snapshotUrl,
    
    @Schema(description = "GPS Latitude")
    BigDecimal latitude,
    
    @Schema(description = "GPS Longitude")
    BigDecimal longitude,
    
    @Schema(description = "Human-readable address or landmark")
    String address
) {}