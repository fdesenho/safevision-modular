package com.safevision.alertservice.service;

import com.safevision.alertservice.dto.AlertEventDTO;
import com.safevision.alertservice.dto.AlertResponse;
import com.safevision.alertservice.model.Alert;
import com.safevision.alertservice.repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for managing Alert business logic.
 * Handles persistence, real-time updates (WebSocket), and critical notifications (Telephony).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlertService {

    private final AlertRepository repository;
    private final TelephonyService telephonyService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Processes a new alert event received from RabbitMQ or HTTP.
     * 1. Persists to Database.
     * 2. Pushes to Frontend via WebSocket.
     * 3. Triggers external notification if CRITICAL.
     *
     * @param event The DTO containing alert details.
     */
    public void createAlert(AlertEventDTO event) {
        log.info("🛡️ Processing new alert event for user: {} | Type: {}", event.userId(), event.alertType());

        // 1. Build and Save Entity
        var alert = Alert.builder()
                .userId(event.userId())
                .alertType(event.alertType())
                .description(event.description())
                .severity(event.severity() != null ? event.severity() : "INFO")
                .cameraId(event.cameraId())
                .snapshotUrl(event.snapshotUrl())
                .acknowledged(false)
                .build();

        var savedAlert = repository.save(alert);
        log.debug("✅ Alert persisted with ID: {}", savedAlert.getId());

        // 2. Convert to Response DTO
        var response = toResponse(savedAlert);

        // 3. Push via WebSocket (Real-time)
        var topic = "/topic/alerts/" + event.userId();
        try {
            messagingTemplate.convertAndSend(topic, response);
            log.info("📡 WebSocket notification sent to topic: {}", topic);
        } catch (Exception e) {
            log.error("❌ Failed to send WebSocket notification: {}", e.getMessage());
        }

        // 4. Trigger Telephony Service (Immediate Reaction)
        if ("CRITICAL".equalsIgnoreCase(event.severity())) {
            log.warn("🚨 CRITICAL severity detected! Triggering Telephony Service for camera: {}", event.cameraId());
            telephonyService.sendCriticalSms(event);
        }
    }

    /**
     * Retrieves alerts for a specific user.
     *
     * @param userId     The user UUID.
     * @param onlyUnread If true, filters out acknowledged alerts.
     * @return List of AlertResponse DTOs.
     */
    public List<AlertResponse> getUserAlerts(String userId, boolean onlyUnread) {
        log.debug("Fetching alerts for user: {} (Unread Only: {})", userId, onlyUnread);

        List<Alert> alerts;

        if (onlyUnread) {
            alerts = repository.findByUserIdAndAcknowledgedFalseOrderByCreatedAtDesc(userId);
        } else {
            alerts = repository.findByUserIdOrderByCreatedAtDesc(userId);
        }

        log.info("Found {} alerts for user: {}", alerts.size(), userId);

        return alerts.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Marks an alert as read/acknowledged.
     * Ensures the user owns the alert before modifying it.
     *
     * @param alertId The UUID of the alert.
     * @param userId  The UUID of the current user.
     * @return true if successful, false otherwise.
     */
    @Transactional
    public boolean acknowledgeAlert(String alertId, String userId) {
        log.info("Attempting to acknowledge alert ID: {} for user: {}", alertId, userId);

        return repository.findById(alertId)
                .filter(alert -> alert.getUserId().equals(userId))
                .map(alert -> {
                    alert.setAcknowledged(true);
                    repository.save(alert);
                    log.info("✅ Alert {} successfully acknowledged.", alertId);
                    return true;
                })
                .orElseGet(() -> {
                    log.warn("❌ Failed to acknowledge. Alert {} not found or access denied for user {}", alertId, userId);
                    return false;
                });
    }

    /**
     * Mapper method to convert Entity to DTO.
     */
    private AlertResponse toResponse(Alert alert) {
        return new AlertResponse(
                alert.getId(),
                alert.getAlertType(),
                alert.getDescription(),
                alert.getSeverity(),
                alert.getCameraId(),
                alert.isAcknowledged(),
                alert.getCreatedAt(),
                alert.getSnapshotUrl()
        );
    }
}