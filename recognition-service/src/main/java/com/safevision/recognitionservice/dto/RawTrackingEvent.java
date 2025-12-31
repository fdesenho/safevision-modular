package com.safevision.recognitionservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;

@Schema(description = "Raw telemetry from the Vision Agent (Edge Computing)")
@JsonIgnoreProperties(ignoreUnknown = true)
public record RawTrackingEvent(
    @Schema(description = "Unique ID for the AI detection session")
    String detectionId,
    
    @Schema(description = "Unix timestamp of the capture")
    long timestamp,
    
    @Schema(description = "Hardware ID of the camera")
    String cameraId,
    
    @Schema(description = "Owner of the monitoring device")
    String userId,
    
    @Schema(description = "True if subject is staring at the camera lens")
    boolean isFacingCamera,
    
    @Schema(description = "Estimated eye-gaze direction", example = "LEFT")
    String gazeDirection,
    
    @Schema(description = "Proximity index (0-100)")
    int depthPosition,
    
    @Schema(description = "Threat flag: Weapon detection status")
    boolean hasWeapon,
    
    @Schema(description = "Type of weapon if detected", example = "FIREARM")
    String weaponType,
    
    @Schema(description = "Weapon placement relative to the body", example = "RIGHT_HAND")
    String weaponLocation,
    
    @Schema(description = "Evidence image URL")
    String snapshotUrl,
    
    @Schema(description = "Edge GPS Latitude")
    BigDecimal latitude,
    
    @Schema(description = "Edge GPS Longitude")
    BigDecimal longitude
) {
    public RawTrackingEvent {
        if (detectionId == null || detectionId.isBlank()) throw new IllegalArgumentException("Detection ID cannot be null");
        if (userId == null || userId.isBlank()) throw new IllegalArgumentException("User ID cannot be null");
    }
}