package com.safevision.alertservice.service;

import com.safevision.alertservice.dto.AlertEventDTO;
import com.safevision.alertservice.dto.AlertResponse;
import com.safevision.alertservice.model.Alert;
import com.safevision.alertservice.repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AlertService {

    private final AlertRepository repository;
    private final TelephonyService telephonyService; // NOVO: Serviço de Telefone
    // ... outros services ...

   

    // Método chamado pelo RabbitMQ ou Controller
    public void createAlert(AlertEventDTO event) {
        Alert alert = Alert.builder()
                .userId(event.userId())
                .alertType(event.alertType())
                .description(event.description())
                .severity(event.severity() != null ? event.severity() : "INFO")
                .cameraId(event.cameraId())
                .acknowledged(false)
                .build();

        repository.save(alert);
        if ("CRITICAL".equalsIgnoreCase(event.severity())) {
            telephonyService.sendCriticalSms(event); // Chamada de API de Produção
        }
    }

    // Busca alertas e converte para DTO
    public List<AlertResponse> getUserAlerts(String userId, boolean onlyUnread) {
        List<Alert> alerts;
        
        if (onlyUnread) {
            alerts = repository.findByUserIdAndAcknowledgedFalseOrderByCreatedAtDesc(userId);
        } else {
            alerts = repository.findByUserIdOrderByCreatedAtDesc(userId);
        }

        // Transforma Entity em DTO
        return alerts.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

   
    @Transactional
    public boolean acknowledgeAlert(String alertId, String userId) {
        return repository.findById(alertId)
                .filter(alert -> alert.getUserId().equals(userId)) // Segurança: Verifica dono
                .map(alert -> {
                    alert.setAcknowledged(true);
                    repository.save(alert);
                    return true;
                })
                .orElse(false);
    }

    // Método auxiliar para converter (Mapper)
    private AlertResponse toResponse(Alert alert) {
        return new AlertResponse(
                alert.getId(),
                alert.getAlertType(),
                alert.getDescription(),
                alert.getSeverity(),
                alert.getCameraId(),
                alert.isAcknowledged(),
                alert.getCreatedAt()
        );
    }
}