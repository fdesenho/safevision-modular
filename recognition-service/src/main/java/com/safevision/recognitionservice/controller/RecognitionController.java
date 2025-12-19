package com.safevision.recognitionservice.controller;

import com.safevision.recognitionservice.dto.AlertEventDTO;
import com.safevision.recognitionservice.producer.AlertProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for external interaction with the Recognition Service.
 * <p>
 * Primarily used for health checks, configuration, and manual simulation
 * of the alert pipeline without requiring a physical camera or Python Agent.
 * </p>
 */
@Slf4j
@RestController
@RequestMapping("/recognition")
@RequiredArgsConstructor
public class RecognitionController {

    private final AlertProducer alertProducer;

    /**
     * Manually triggers a detection event and sends it directly to the Alert Service queue.
     * <p>
     * This endpoint is useful for integration testing (RabbitMQ -> Alert Service -> Frontend)
     * independently of the Edge/Python layer.
     * </p>
     *
     * @return Confirmation message.
     */
    @PostMapping("/simulate")
    public ResponseEntity<String> simulateDetection() {
        log.info("🔄 Manual simulation triggered via HTTP endpoint.");

        // 1. Create a CRITICAL test DTO
        // Adjusted constructor to match the new DTO signature with GPS fields.
        var testAlert = new AlertEventDTO(
            "superadmin",       // userId
            "MANUAL_TRIGGER",   // alertType
            "Manual API test triggered via HTTP endpoint.", // description
            "CRITICAL",         // severity
            "CAM-TEST-00",      // cameraId
            null,               // snapshotUrl (no image for manual test)
            null,               // latitude (no GPS for manual test)
            null,               // longitude
            null                // address
        );

        // 2. Dispatch to RabbitMQ
        alertProducer.sendAlert(testAlert);

        log.info("✅ Simulation sent to queue successfully.");
        return ResponseEntity.ok("Manual simulation successfully sent to Alert queue.");
    }
}