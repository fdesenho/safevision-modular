package com.safevision.alertservice.integration;

import static org.awaitility.Awaitility.await;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import com.safevision.alertservice.AbstractIntegrationTest;
import com.safevision.alertservice.dto.AlertEventDTO;
import com.safevision.alertservice.repository.AlertRepository;

class AlertMessagingIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private AlertRepository alertRepository;

    @Test
    @DisplayName("Should process alert message from RabbitMQ and persist in DB")
    void shouldProcessMessageFromRabbit() {
        // 1. Arrange: Prepara o evento
        var event = new AlertEventDTO(
            "user-rabbit-999", "INTRUSION", "Rabbit Test", "HIGH", 
            "CAM-X", null, new BigDecimal("-27.0"), new BigDecimal("-48.0"), null
        );

        // 2. Act: Envia para a fila que o seu Listener escuta
        rabbitTemplate.convertAndSend("safevision.alerts", event);

        // 3. Assert: Como é assíncrono, aguardamos até 5 segundos para o dado aparecer no banco
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            var alerts = alertRepository.findByUserIdOrderByCreatedAtDesc("user-rabbit-999");
            assertThat(alerts).isNotEmpty();
            assertThat(alerts.get(0).getAlertType()).isEqualTo("INTRUSION");
        });
    }
}