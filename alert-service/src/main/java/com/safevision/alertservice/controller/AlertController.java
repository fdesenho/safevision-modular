package com.safevision.alertservice.controller;

import com.safevision.alertservice.dto.AlertEventDTO;
import com.safevision.alertservice.dto.AlertResponse;
import com.safevision.alertservice.service.AlertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/alert")
@RequiredArgsConstructor
@Tag(name = "Alert Management", description = "Endpoints for managing security alerts and notification history")
public class AlertController {

    private final AlertService alertService;

    @Operation(summary = "Push HTTP Event", description = "Manual entry point for alert events (Fallback for RabbitMQ).")
    @PostMapping("/event")
    public ResponseEntity<Void> receiveEvent(@RequestBody AlertEventDTO event) {
        if (isValidEvent(event)) {
            return ResponseEntity.badRequest().build();
        }
        alertService.createAlert(event);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "List User Alerts", description = "Retrieves all security alerts for the currently authenticated user.")
    @GetMapping("/user/{username}")
    public ResponseEntity<List<AlertResponse>> getMyAlerts(
            @Parameter(hidden = true) Authentication authentication,
            @Parameter(description = "Filter only unread alerts") @RequestParam(defaultValue = "false") boolean unreadOnly) {
        var userId = authentication.getName();
        return ResponseEntity.ok(alertService.getUserAlerts(userId, unreadOnly));
    }

    @Operation(summary = "Acknowledge Alert", description = "Marks a specific alert as read/resolved.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Alert acknowledged"),
        @ApiResponse(responseCode = "404", description = "Alert not found or unauthorized")
    })
    @PatchMapping("/{id}/ack")
    public ResponseEntity<Void> acknowledgeAlert(
            @PathVariable String id,
            @Parameter(hidden = true) Authentication authentication) {
        var userId = authentication.getName();
        return alertService.acknowledgeAlert(id, userId) 
                ? ResponseEntity.noContent().build() 
                : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @Operation(summary = "Get Alert History", description = "Paginated history of security events for a user.")
    @GetMapping("/history/{userId}")
    public ResponseEntity<Page<AlertResponse>> getHistory(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(alertService.getUserAlertsPaginated(userId, page, size));
    }

    private boolean isValidEvent(AlertEventDTO event) {
        return event == null || event.userId() == null || event.alertType() == null;
    }
}