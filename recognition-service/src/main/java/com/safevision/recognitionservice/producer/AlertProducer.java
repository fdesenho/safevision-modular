package com.safevision.recognitionservice.producer;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value; // Importar @Value
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import com.safevision.recognitionservice.dto.AlertEventDTO;
import com.safevision.recognitionservice.service.MovementHistoryService;

import lombok.RequiredArgsConstructor;

/**
 * Producer class responsible for sending final, filtered alerts to the RabbitMQ output queue.
 * Adheres to the Single Responsibility Principle (SRP) by focusing only on communication.
 */
@Service
@RequiredArgsConstructor
public class AlertProducer {

    private final RabbitTemplate rabbitTemplate;
    private final String alertsQueueName; 
    
   
    
    /**
     * Sends the final filtered alert DTO to the output queue.
     */
    public void sendAlert(AlertEventDTO finalAlert) {
        try {
        	System.out.println("✅ [AlertProducer] Initial alert sent to queue.");
            rabbitTemplate.convertAndSend(alertsQueueName, finalAlert);

            System.out.println("✅ [AlertProducer] Final alert sent to queue.");

        } catch (Exception e) {
            System.err.println("❌ Error posting final ALERT to RabbitMQ: " + e.getMessage());
        }
    }
}