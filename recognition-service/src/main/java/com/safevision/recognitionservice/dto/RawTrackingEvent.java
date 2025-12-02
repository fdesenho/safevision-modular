package com.safevision.recognitionservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RawTrackingEvent(
    String detectionId,
    long timestamp,
    boolean isFacingCamera,
    String gazeDirection,
    String cameraId,
    String userId,
    int depthPosition,
    // NOVOS CAMPOS
    boolean hasWeapon,
    String weaponType,
    String weaponLocation
) {}