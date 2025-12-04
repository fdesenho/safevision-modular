package com.safevision.recognitionservice.dto;

/**
 * Data Transfer Object (DTO) representing a finalized Alert Event.
 * <p>
 * This record serves as the contract for messages sent to the <b>Alert Service</b> via RabbitMQ.
 * It contains the filtered and confirmed threat information, ready for persistence and notification.
 * </p>
 *
 * @param userId      The UUID of the user to whom this alert belongs.
 * @param alertType   The classification of the threat (e.g., "WEAPON_DETECTED", "THREAT_STARE").
 * @param description A human-readable description of the event for the UI/SMS.
 * @param severity    The risk level (e.g., "CRITICAL", "WARNING").
 * @param cameraId    The identifier of the camera that captured the event.
 * @param snapshotUrl The public MinIO URL pointing to the visual evidence (image) of the threat.
 */
public record AlertEventDTO(
    String userId,
    String alertType,
    String description,
    String severity,
    String cameraId,
    String snapshotUrl
) {}