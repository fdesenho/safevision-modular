package com.safevision.recognitionservice.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Data Transfer Object (DTO) representing the raw telemetry received from the Vision Agent.
 * <p>
 * This Java Record serves as an immutable carrier for the data coming from RabbitMQ.
 * It includes behavioral analysis, threat detection, and geolocation metadata.
 * </p>
 *
 * @param detectionId    Unique UUID for the detection session.
 * @param timestamp      Unix timestamp of capture.
 * @param cameraId       Identifier of the recording device.
 * @param userId         The user associated with the device.
 * @param isFacingCamera Behavioral flag: is the subject looking at the lens?
 * @param gazeDirection  Estimated direction of gaze.
 * @param depthPosition  Proximity score.
 * @param hasWeapon      Threat flag: was a weapon detected?
 * @param weaponType     Classification of the weapon (e.g., KNIFE).
 * @param weaponLocation Relative location of the weapon (e.g., HAND).
 * @param snapshotUrl    MinIO URL for the evidence image.
 * @param latitude       GPS Latitude (nullable if no signal).
 * @param longitude      GPS Longitude (nullable if no signal).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record RawTrackingEvent(
    String detectionId,
    long timestamp,
    String cameraId,
    String userId,
    boolean isFacingCamera,
    String gazeDirection,
    int depthPosition,
    boolean hasWeapon,
    String weaponType,
    String weaponLocation,
    String snapshotUrl,
    BigDecimal latitude,
    BigDecimal longitude
) {
    /**
     * Validation constructor.
     * Ensures critical identity fields are present.
     */
    public RawTrackingEvent {
        if (detectionId == null || detectionId.isBlank()) {
            throw new IllegalArgumentException("Detection ID cannot be null");
        }
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
    }
}