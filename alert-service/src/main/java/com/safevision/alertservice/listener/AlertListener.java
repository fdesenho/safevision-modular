package com.safevision.alertservice.listener;

import com.safevision.alertservice.dto.AlertEventDTO;
import com.safevision.alertservice.service.AlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Listener responsible for consuming processed alerts from the RabbitMQ queue.
 * This component acts as the bridge between the Recognition Service (Producer)
 * and the Alert Service logic (Persistence/Notification).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AlertListener {

    private final AlertService alertService;

    /**
     * Consumes alert events from the queue defined in RabbitMQConfig.
     * Uses SpEL to resolve the queue name bean 'alertsQueueName'.
     *
     * @param event The DTO containing alert details.
     */
    @RabbitListener(queues = "#{alertsQueueName}")
    public void receiveAlert(AlertEventDTO event) {
        log.info("📨 [RabbitMQ] Received alert event type: {}", event.alertType());
        log.debug("Alert payload: {}", event);

        try {
            // Delegate to the service layer for persistence and notification logic
            alertService.createAlert(event);
            
        } catch (Exception e) {
            // Log the full stack trace for debugging purposes
            log.error("❌ Error processing RabbitMQ alert: {}", e.getMessage(), e);

            // Note: In a production environment with DLQ (Dead Letter Queue) configured,
            // you might want to throw a specific AmqpRejectAndDontRequeueException here
            // to move the failed message to the DLQ instead of losing it.
        }
    }
}