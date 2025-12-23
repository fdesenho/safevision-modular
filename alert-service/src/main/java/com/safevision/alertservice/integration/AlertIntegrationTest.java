package com.safevision.alertservice.integration;

import com.safevision.alertservice.dto.AlertEventDTO;
import com.safevision.alertservice.repository.AlertRepository;
import com.safevision.alertservice.service.AlertService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection; 
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class AlertIntegrationTest {

   
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Container
    @ServiceConnection
    static RabbitMQContainer rabbitmq = new RabbitMQContainer("rabbitmq:3-management");

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
       
        var alertEvent = new AlertEventDTO(
            "user-123",
            "WEAPON_DETECTED",
            "Test description",
            "CRITICAL",
            "CAM-01",
            null, // sem foto
            new BigDecimal("-27.59"),
            new BigDecimal("-48.56"),
            null
        );

        // 2. When (Quando o serviço processa)
        alertService.processAlert(alertEvent);

        // 3. Then (Então deve estar no banco)
        // Awaitility é útil porque RabbitMQ é assíncrono, mas como chamamos o service direto,
        // podemos validar o banco imediatamente neste cenário.
        var alerts = alertRepository.findAll();
        
        assertThat(alerts).hasSize(1);
        assertThat(alerts.getFirst().getAlertType()).isEqualTo("WEAPON_DETECTED");
        assertThat(alerts.getFirst().getUserId()).isEqualTo("user-123");
    }
}