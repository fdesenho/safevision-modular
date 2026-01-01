package com.safevision.alertservice.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.safevision.alertservice.AbstractIntegrationTest;
import com.safevision.alertservice.dto.AlertEventDTO;
import com.safevision.alertservice.repository.AlertRepository;
import com.safevision.alertservice.service.AlertService;

// 👇 Agora estende a classe base e herda os containers
class AlertIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private AlertService alertService;

    @Autowired
    private AlertRepository alertRepository;

    @BeforeEach
    void setup() {
        alertRepository.deleteAll(); 
    }

    @Test
    @DisplayName("Should persist alert via Service layer interacting with real DB")
    void shouldPersistAlert() {
        // 1. Given (Cenário)
        var alertEvent = new AlertEventDTO(
            "user-123",
            "WEAPON_DETECTED",
            "Test description",
            "CRITICAL",
            "CAM-01",
            null, 
            new BigDecimal("-27.59"),
            new BigDecimal("-48.56"),
            null
        );

        // 2. When (Ação)
        alertService.createAlert(alertEvent);

        // 3. Then (Validação no Banco Real)
        var alerts = alertRepository.findAll();
        
        assertThat(alerts).hasSize(1);
        var savedAlert = alerts.getFirst();
        
        assertThat(savedAlert.getAlertType()).isEqualTo("WEAPON_DETECTED");
        assertThat(savedAlert.getUserId()).isEqualTo("user-123");
        
        // Validação extra importante para GeoLocation:
        assertThat(savedAlert.getLatitude()).isEqualByComparingTo(new BigDecimal("-27.59"));
    }
}