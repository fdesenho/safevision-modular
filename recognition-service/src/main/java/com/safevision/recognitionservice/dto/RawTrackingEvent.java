package com.safevision.recognitionservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Data Transfer Object (DTO) representing the raw telemetry data received from the Vision Agent.
 * <p>
 * This record maps the JSON payload sent by the Python script via RabbitMQ.
 * It contains a mix of behavioral data (gaze, proximity) and object detection data (weapons).
 * </p>
 * * @param detectionId    Unique identifier for the tracking session/object.
 * @param timestamp      Unix timestamp (seconds) of the event occurrence.
 * @param cameraId       Identifier of the source camera (e.g., "BODY-CAM-01").
 * @param userId         The ID of the user utilizing the device.
 * @param isFacingCamera True if the subject's face is oriented towards the camera.
 * @param gazeDirection  Estimated direction of the gaze (e.g., "CENTER", "LEFT").
 * @param depthPosition  A calculated score (0-100+) indicating proximity based on face width.
 * @param hasWeapon      True if a threat object (knife/gun) was detected via YOLO.
 * @param weaponType     The label of the detected weapon (e.g., "KNIFE", "HANDGUN").
 * @param weaponLocation The estimated position of the weapon relative to the body (e.g., "HAND", "WAIST").
 * @param snapshotUrl    The MinIO public URL for the evidence image (can be null if no upload occurred).
 */
@JsonIgnoreProperties(ignoreUnknown = true) 
public record RawTrackingEvent(
    // --- Metadata ---
    String detectionId,
    long timestamp,
    String cameraId,
    String userId,

    // --- Behavioral Analysis Data ---
    boolean isFacingCamera,
    String gazeDirection,
    int depthPosition,

    // --- Threat Detection Data (Zero Tolerance) ---
    boolean hasWeapon,
    String weaponType,
    String weaponLocation,

    // --- Evidence ---
    String snapshotUrl
) {
    
    /**
     * Compact Constructor for validation.
     * Ensures that critical identification fields are never null.
     */
    public RawTrackingEvent {
        if (detectionId == null || detectionId.isBlank()) {
            throw new IllegalArgumentException("Detection ID cannot be null or empty");
        }
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }
    }
}