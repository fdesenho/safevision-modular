package com.safevision.alertservice.controller;

import com.safevision.alertservice.dto.AlertEventDTO;
import com.safevision.alertservice.dto.AlertResponse;
import com.safevision.alertservice.service.AlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for managing Alerts.
 * Exposed via API Gateway.
 * Handles both machine-generated events (fallback/testing) and user interactions.
 */
@Slf4j
@RestController
@RequestMapping("/alert") // Matches Gateway route predicate
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    /**
     * Endpoint for internal services or testing tools to push alerts via HTTP.
     * Note: In production, the primary flow is asynchronous via RabbitMQ.
     *
     * @param event The alert data.
     * @return 201 Created if successful, 400 Bad Request if invalid.
     */
    @PostMapping("/event")
    public ResponseEntity<Void> receiveEvent(@RequestBody AlertEventDTO event) {
        log.debug("Received HTTP alert event request: {}", event);

        if (isValidEvent(event)) {
            log.warn("Rejected invalid HTTP alert event: Missing required fields.");
            return ResponseEntity.badRequest().build();
        }

        alertService.createAlert(event);
        log.info("Alert created successfully via HTTP for user: {}", event.userId());

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Retrieves alerts for the authenticated user.
     *
     * @param authentication The security context token.
     * @param unreadOnly     Query param to filter only unread alerts.
     * @return List of alerts.
     */
    @GetMapping("/user/{username}")
    public ResponseEntity<List<AlertResponse>> getMyAlerts(
            Authentication authentication,
            @RequestParam(defaultValue = "false") boolean unreadOnly) {

        var userId = authentication.getName();
        log.debug("Fetching alerts for user: {} [UnreadOnly: {}]", userId, unreadOnly);

        var alerts = alertService.getUserAlerts(userId, unreadOnly);

        return ResponseEntity.ok(alerts);
    }

    /**
     * Marks a specific alert as acknowledged (read).
     *
     * @param id             The Alert UUID.
     * @param authentication The security context token.
     * @return 204 No Content if successful, 404 if not found or unauthorized.
     */
    @PatchMapping("/{id}/ack")
    public ResponseEntity<Void> acknowledgeAlert(
            @PathVariable String id,
            Authentication authentication) {

        var userId = authentication.getName();
        log.debug("Request to acknowledge alert ID: {} by user: {}", id, userId);

        boolean success = alertService.acknowledgeAlert(id, userId);

        if (success) {
            log.info("Alert {} acknowledged by user {}", id, userId);
            return ResponseEntity.noContent().build();
        } else {
            log.warn("Failed to acknowledge alert {}. Not found or permission denied.", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Helper method to validate the incoming event DTO.
     */
    private boolean isValidEvent(AlertEventDTO event) {
        return event == null || event.userId() == null || event.alertType() == null;
    }
}