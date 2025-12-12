package com.safevision.alertservice.service;

import com.safevision.alertservice.client.AlertPreferenceClient;
import com.safevision.alertservice.dto.AlertEventDTO;
import com.safevision.alertservice.dto.AlertResponse;
import com.safevision.alertservice.model.Alert;
import com.safevision.alertservice.repository.AlertRepository;
import com.safevision.common.enums.AlertType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service layer for managing Alert business logic.
 * Handles persistence, real-time updates via WebSocket,
 * and triggers external notifications when severity is CRITICAL.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlertService {

    private final AlertRepository repository;
    private final TelephonyService telephonyService;
    private final SimpMessagingTemplate messagingTemplate;
    private final TelegramService telegramService;
    private final EmailAlertService emailService;

    // 🔥 Novo: consultar preferências via Feign Client
    private final AlertPreferenceClient preferenceClient;


    /**
     * Processes a new alert event received from RabbitMQ or HTTP.
     */
    public void createAlert(AlertEventDTO event) {
        log.info("🛡️ Processing alert for user {} | Type {}", event.userId(), event.alertType());

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

        sendWebSocket(event.userId(), savedAlert);

        if ("CRITICAL".equalsIgnoreCase(event.severity())) {
            Set<AlertType> preferences = getUserPreferences(event.userId());
            triggerCriticalNotifications(event, preferences);
        }
    }


  
    private Set<AlertType> getUserPreferences(String userId) {
        Set<AlertType> response = new HashSet<>();

        try {
            List<AlertType> types = preferenceClient.getPreferences(userId);

            if (types != null) {
                response.addAll(types);
            }

        } catch (Exception e) {
            log.error("❌ Failed to fetch alert preferences for user {}. Error: {}",
                    userId, e.getMessage());
        }

        return response;
    }



    private void sendWebSocket(String userId, Alert savedAlert) {
        var response = toResponse(savedAlert);
        var topic = "/topic/alert/" + userId;

        try {
            messagingTemplate.convertAndSend(topic, response);
            log.info("📡 WebSocket pushed to {}", topic);
        } catch (Exception e) {
            log.error("❌ WebSocket failed: {}", e.getMessage());
        }
    }


    private void triggerCriticalNotifications(AlertEventDTO event, Set<AlertType> prefs) {
       
    	if (prefs.contains(AlertType.SMS)) {
            telephonyService.sendCriticalSms(event);
        }
        if (prefs.contains(AlertType.TELEGRAM)) {
            telegramService.sendAlert(event);
        }
        if (prefs.contains(AlertType.EMAIL)) {
            emailService.sendHtmlAlert(event);
        }
    }


    /**
     * Retrieves alerts for a user.
     */
    public List<AlertResponse> getUserAlerts(String userId, boolean onlyUnread) {
        log.debug("Fetching alerts for user {} | unread={}", userId, onlyUnread);

        List<Alert> alerts = onlyUnread
                ? repository.findByUserIdAndAcknowledgedFalseOrderByCreatedAtDesc(userId)
                : repository.findByUserIdOrderByCreatedAtDesc(userId);

        return alerts.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }


    /**
     * Acknowledge alert.
     */
    @Transactional
    public boolean acknowledgeAlert(String alertId, String userId) {
        return repository.findById(alertId)
                .filter(a -> a.getUserId().equals(userId))
                .map(a -> {
                    a.setAcknowledged(true);
                    repository.save(a);
                    return true;
                })
                .orElse(false);
    }


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
