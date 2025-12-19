package com.safevision.alertservice.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.safevision.alertservice.client.AlertPreferenceClient;
import com.safevision.alertservice.dto.AlertEventDTO;
import com.safevision.alertservice.dto.AlertResponse;
import com.safevision.alertservice.model.Alert;
import com.safevision.alertservice.repository.AlertRepository;
import com.safevision.common.enums.AlertType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service layer for managing Alert business logic.
 * <p>
 * Responsibilities:
 * 1. Low-latency persistence of threats.
 * 2. Async data enrichment (Reverse Geocoding).
 * 3. Real-time WebSocket push.
 * </p>
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
    private final AlertPreferenceClient preferenceClient;
    
    // 📍 New Dependency for Reverse Geocoding
    private final GeocodingService geocodingService;

   
    public void createAlert(AlertEventDTO event) {
        log.info("🛡️ Processing alert for user {} | Type {}", event.userId(), event.alertType());

        // 1. Build Initial Entity
        var alert = Alert.builder()
                .userId(event.userId())
                .alertType(event.alertType())
                .description(event.description())
                .severity(event.severity() != null ? event.severity() : "INFO")
                .cameraId(event.cameraId())
                .snapshotUrl(event.snapshotUrl())
                .latitude(event.latitude())
                .longitude(event.longitude())
                .acknowledged(false)
                .build();

        // 2. Persist Immediately
        var savedAlert = repository.save(alert);

        // 3. Lógica de Envio Único (Fluxo Bifurcado)
        if (event.latitude() != null && event.longitude() != null) {
            // CASO A: Tem coordenadas -> Busca Endereço -> Atualiza -> Envia
            geocodingService.getAddressFromCoordinates(event.latitude(), event.longitude())
                .thenAccept(address -> {
                    if (address != null) {
                        savedAlert.setAddress(address);
                        repository.save(savedAlert);
                        log.debug("📍 Address updated for alert {}: {}", savedAlert.getId(), address);
                    }
                    // ✅ ENVIO 1 (Com endereço)
                    dispatchNotifications(event, savedAlert);
                })
                .exceptionally(ex -> {
                    log.error("⚠️ Geocoding failed, sending alert without address.", ex);
                    // ✅ ENVIO 1 (Fallback em caso de erro no Geo)
                    dispatchNotifications(event, savedAlert);
                    return null;
                });
        } else {
            // CASO B: Não tem coordenadas -> Envia Imediatamente
            // ✅ ENVIO 1 (Sem endereço)
            dispatchNotifications(event, savedAlert);
        }
    }

    /**
     * Centraliza o envio de WebSocket e Notificações Externas
     * para garantir que tudo ocorra no momento certo.
     */
    private void dispatchNotifications(AlertEventDTO event, Alert alertEntity) {
        // 1. Push WebSocket (Agora acontece uma única vez)
        sendWebSocket(event.userId(), alertEntity);

        // 2. Critical Notifications (SMS, Email, Telegram)
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
            log.error("❌ Failed to fetch alert preferences for user {}. Error: {}", userId, e.getMessage());
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

    public List<AlertResponse> getUserAlerts(String userId, boolean onlyUnread) {
        log.debug("Fetching alerts for user {} | unread={}", userId, onlyUnread);
        List<Alert> alerts = onlyUnread
                ? repository.findByUserIdAndAcknowledgedFalseOrderByCreatedAtDesc(userId)
                : repository.findByUserIdOrderByCreatedAtDesc(userId);

        return alerts.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

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
                alert.getSnapshotUrl(),
                // 📍 Include Geolocation Data in Response
                alert.getLatitude(),
                alert.getLongitude(),
                alert.getAddress()
        );
    }
    public Page<AlertResponse> getUserAlertsPaginated(String userId, int page, int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        
        return repository.findByUserId(userId, pageable)
                .map(this::toResponse); // Conversão Entity -> DTO
    }
}