package com.safevision.recognitionservice.producer;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import com.safevision.recognitionservice.dto.AlertEventDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Producer class responsible for sending final, filtered alerts to the RabbitMQ output queue.
 * <p>
 * Adheres to the Single Responsibility Principle (SRP) by focusing only on the
 * communication layer with the Message Broker.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlertProducer {

    private final RabbitTemplate rabbitTemplate;
    private final String alertsQueueName;

   
    /**
     * Sends the final filtered alert DTO to the output queue.
     *
     * @param finalAlert The DTO containing the threat details and evidence URL.
     */
    public void sendAlert(AlertEventDTO finalAlert) {
        log.debug("Attempting to send alert to queue: {}", alertsQueueName);

        try {
            rabbitTemplate.convertAndSend(alertsQueueName, finalAlert);
            log.info("✅ [AlertProducer] Critical Alert dispatched to RabbitMQ successfully.");

        } catch (Exception e) {
            log.error("❌ Error publishing alert to RabbitMQ: {}", e.getMessage(), e);
        }
    }
}