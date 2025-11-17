package com.safevision.alertservice.service;

import com.safevision.alertservice.model.Alert;
import com.safevision.alertservice.repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AlertService {

    private final AlertRepository repository;

    public Alert createAlert(Alert alert) {
        if (alert.getId() == null) {
            alert.setId(java.util.UUID.randomUUID().toString());
        }
        if (alert.getCreatedAt() == null) {
            alert.setCreatedAt(Instant.now());
        }
        alert.setAcknowledged(false);
        return repository.save(alert);
    }

    public Optional<Alert> getAlert(String id) {
        return repository.findById(id);
    }

    public List<Alert> getAlertsForUser(String userId) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Alert> getUnreadAlertsForUser(String userId) {
        return repository.findByUserIdAndAcknowledgedFalseOrderByCreatedAtDesc(userId);
    }

    public Optional<Alert> acknowledge(String id, String userId) {
        return repository.findById(id)
                .map(alert -> {
                    if (!alert.getUserId().equals(userId)) {
                        return null; // ownership mismatch
                    }
                    alert.setAcknowledged(true);
                    return repository.save(alert);
                });
    }

    // Additional helper for internal event: create and return alert
    public Alert createFromEvent(String userId, String alertType, String cameraId, String description, String severity) {
        Alert a = new Alert();
        a.setUserId(userId);
        a.setAlertType(alertType);
        a.setCameraId(cameraId);
        a.setDescription(description);
        a.setSeverity(severity);
        a.setCreatedAt(Instant.now());
        a.setAcknowledged(false);
        return createAlert(a);
    }
}
