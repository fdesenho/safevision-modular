package com.safevision.alertservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Complete alert details for history and frontend display")
public record AlertResponse(
    @Schema(description = "Unique alert ID", example = "alt-12345")
    String id,
    
    @Schema(description = "Type of threat detected", example = "WEAPON_DETECTED")
    String alertType,
    
    @Schema(description = "Detailed human-readable description", example = "A person with a firearm was detected at the main gate.")
    String description,
    
    @Schema(description = "Risk level", example = "CRITICAL")
    String severity,
    
    @Schema(description = "Source camera identifier", example = "CAM-01-ENTRANCE")
    String cameraId,
    
    @Schema(description = "Flag indicating if the user has read/acknowledged the alert")
    boolean acknowledged,
    
    @Schema(description = "Timestamp of event creation")
    LocalDateTime createdAt,
    
    @Schema(description = "URL for the evidence snapshot stored in MinIO", example = "https://cdn.safevision.com/snaps/alt-123.jpg")
    String snapshotUrl,
    
    @Schema(description = "Geographical latitude", example = "-27.595377")
    BigDecimal latitude,
    
    @Schema(description = "Geographical longitude", example = "-48.548050")
    BigDecimal longitude,
    
    @Schema(description = "Physical address of the event", example = "Hercilio Luz Bridge, Florianopolis, SC")
    String address
) {}