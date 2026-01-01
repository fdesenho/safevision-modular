package com.safevision.alertservice.integration; // Pacote onde o teste está

// 1. IMPORTANTE: Imports estáticos para Mockito e AssertJ
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;

// 2. IMPORTANTE: Importar a classe base que está no pacote de cima
import com.safevision.alertservice.AbstractIntegrationTest;
import com.safevision.alertservice.client.AlertPreferenceClient;
import com.safevision.alertservice.dto.AlertEventDTO;
import com.safevision.alertservice.repository.AlertRepository;
import com.safevision.alertservice.service.AlertService;
import com.safevision.alertservice.service.EmailAlertService;
import com.safevision.alertservice.service.GeocodingService;
import com.safevision.alertservice.service.TelegramService;
import com.safevision.alertservice.service.TelephonyService;
import com.safevision.common.enums.AlertType;

class AlertServiceCoverageTest extends AbstractIntegrationTest {

    @Autowired
    private AlertService alertService;

    @Autowired
    private AlertRepository repository;

    @MockBean private GeocodingService geocodingService;
    @MockBean private AlertPreferenceClient preferenceClient;
    @MockBean private TelephonyService telephonyService;
    @MockBean private TelegramService telegramService;
    @MockBean private EmailAlertService emailService;
    @MockBean private SimpMessagingTemplate messagingTemplate;

    @Test
    @DisplayName("Should cover Full Create Alert Flow")
    void createAlert_FullFlow_Success() {
        var event = new AlertEventDTO("user-1", "FIRE", "Fire", "CRITICAL", "CAM-1", null, 
                                     new BigDecimal("-27.5"), new BigDecimal("-48.5"), null);
        
        when(geocodingService.getAddressFromCoordinates(any(), any()))
            .thenReturn(CompletableFuture.completedFuture("Rua Teste"));
        
        when(preferenceClient.getPreferences("user-1"))
            .thenReturn(List.of(AlertType.SMS));

        alertService.createAlert(event);

        // findByUserId no seu código exige Pageable
        var alertsPage = repository.findByUserId("user-1", PageRequest.of(0, 10));
        assertThat(alertsPage.getContent()).isNotEmpty();
        
        verify(messagingTemplate, atLeastOnce()).convertAndSend(anyString(), any(Object.class));
    }

    @Test
    @DisplayName("Should cover acknowledge failure")
    void acknowledgeAlert_NotFound() {
        boolean result = alertService.acknowledgeAlert("invalid-id", "any-user");
        assertThat(result).isFalse();
    }
}