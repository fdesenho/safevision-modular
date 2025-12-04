package com.safevision.alertservice.dto;

import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) for returning Alert details to the client (Frontend).
 * <p>
 * This record projects the internal Alert entity into a safe, immutable structure
 * to be serialized as JSON. It includes the evidence URL (snapshot) from MinIO.
 * </p>
 *
 * @param id           The unique UUID of the alert.
 * @param alertType    The classification of the threat (e.g., "WEAPON_DETECTED").
 * @param description  A detailed message describing the event.
 * @param severity     The risk level (INFO, WARNING, CRITICAL).
 * @param cameraId     The identifier of the source camera.
 * @param acknowledged True if the user has marked this alert as read.
 * @param createdAt    The timestamp when the alert was generated.
 * @param snapshotUrl  The public URL to the evidence image (if available).
 */
public record AlertResponse(
    String id,
    String alertType,
    String description,
    String severity,
    String cameraId,
    boolean acknowledged,
    LocalDateTime createdAt,
    String snapshotUrl
) {}