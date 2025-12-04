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
 * Primarily used for health checks, configuration (future), and manual testing
 * of the alert pipeline without needing the Python Agent.
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
     * This endpoint is protected by JWT and useful for testing the RabbitMQ -> Alert flow.
     *
     * @return Confirmation message.
     */
    @PostMapping("/simulate")
    public ResponseEntity<String> simulateDetection() {
        log.info("🔄 Manual simulation triggered via HTTP endpoint.");

        // 1. Create a CRITICAL test DTO
        // Note: snapshotUrl is null because there is no real image evidence for manual tests.
        var testAlert = new AlertEventDTO(
            "superadmin",
            "MANUAL_TRIGGER",
            "Manual API test triggered via HTTP endpoint.",
            "CRITICAL",
            "CAM-TEST-00",
            null 
        );

        // 2. Dispatch to RabbitMQ
        alertProducer.sendAlert(testAlert);

        log.info("✅ Simulation sent to queue successfully.");
        return ResponseEntity.ok("Manual simulation successfully sent to Alert queue.");
    }
}