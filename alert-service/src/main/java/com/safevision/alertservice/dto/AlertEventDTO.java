package com.safevision.alertservice.dto;

/**
 * Data Transfer Object (DTO) representing an Alert Event.
 * <p>
 * This object acts as the contract between the Recognition Service (Producer)
 * and the Alert Service (Consumer). It carries the threat analysis results
 * and the evidence link.
 * </p>
 *
 * @param userId      The UUID of the user who owns the monitoring session.
 * @param alertType   The classification of the event (e.g., "WEAPON_DETECTED", "THREAT_STARE").
 * @param description A human-readable description of what happened.
 * @param severity    The risk level (INFO, WARNING, CRITICAL).
 * @param cameraId    The identifier of the camera (physical or virtual) that captured the event.
 * @param snapshotUrl The public URL pointing to the evidence image stored in MinIO (can be null).
 */
public record AlertEventDTO(
    String userId,
    String alertType,
    String description,
    String severity,
    String cameraId,
    String snapshotUrl
) {}