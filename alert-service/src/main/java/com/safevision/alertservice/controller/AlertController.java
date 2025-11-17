package com.safevision.alertservice.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.safevision.alertservice.model.Alert;
import com.safevision.alertservice.service.AlertService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    // internal token used to accept events from recognition-service
    @Value("${internal.token}")
    private String internalToken;

    // -------------------------
    // Internal endpoint used by recognition-service (or other internal services)
    // Must provide header: X-Internal-Token: <internalToken>
    // -------------------------
    public static record AlertEventRequest(
            String userId,
            String alertType,
            String cameraId,
            String description,
            String severity
    ) {}

    @PostMapping("/event")
    public ResponseEntity<?> receiveEvent(@RequestHeader(value = "X-Internal-Token", required = false) String token,
                                          @RequestBody AlertEventRequest event) {

        if (token == null || !token.equals(internalToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden - invalid internal token");
        }

        if (event == null || event.userId() == null || event.alertType() == null) {
            return ResponseEntity.badRequest().body("Missing userId or alertType");
        }

        Alert alert = alertService.createFromEvent(
                event.userId(),
                event.alertType(),
                event.cameraId(),
                event.description(),
                event.severity() == null ? "INFO" : event.severity());

        // TODO: hook for notifications (email, push, websocket)
        System.out.println("Created alert: " + alert.getId() + " for user " + alert.getUserId());

        return ResponseEntity.status(HttpStatus.CREATED).body(alert);
    }

    // -------------------------
    // Endpoints for users - gateway must forward X-User-Id header (from JWT)
    // -------------------------

    @GetMapping
    public ResponseEntity<List<Alert>> listForUser(@RequestHeader("X-User-Id") String userId) {
        var list = alertService.getAlertsForUser(userId);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/unread")
    public ResponseEntity<List<Alert>> unreadForUser(@RequestHeader("X-User-Id") String userId) {
        var list = alertService.getUnreadAlertsForUser(userId);
        return ResponseEntity.ok(list);
    }

    @PostMapping("/{id}/ack")
    public ResponseEntity<?> acknowledge(@PathVariable String id, @RequestHeader("X-User-Id") String userId) {
        var result = alertService.acknowledge(id, userId);
        if (result.isPresent() && result.get() != null) {
            return ResponseEntity.ok(result.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Alert not found or not permitted");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getAlert(@PathVariable String id, @RequestHeader("X-User-Id") String userId) {
        return alertService.getAlert(id)
                .filter(alert -> alert.getUserId().equals(userId))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found or not permitted"));
    }
}
