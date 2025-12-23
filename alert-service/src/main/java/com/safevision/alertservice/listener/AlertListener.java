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
            
            alertService.createAlert(event);
            
        } catch (Exception e) {
            
            log.error("❌ Error processing RabbitMQ alert: {}", e.getMessage(), e);

            
        }
    }
}