package com.safevision.recognitionservice.listener;

import com.safevision.recognitionservice.dto.RawTrackingEvent;
import com.safevision.recognitionservice.facade.TrackingWorkflowFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Infrastructure Layer: Listens to RabbitMQ messages.
 * Responsible ONLY for deserialization and delegating to the business facade.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VisionAgentListener {

    // Injection of the Facade, decoupling the listener from specific services
    private final TrackingWorkflowFacade trackingWorkflow;

    /**
     * Consumes raw tracking events from the Python Agent.
     * Uses SpEL to resolve the queue name bean dynamically.
     *
     * @param event DTO containing raw tracking data.
     */
    @RabbitListener(queues = "#{rawTrackingQueueName}")
    public void handleRawTrackingEvent(RawTrackingEvent event) {
        
        try {
            log.info("🤖 [Recognition] Received raw event: {}", event.detectionId());
            
            // Delegate to the Facade (Business Logic)
            trackingWorkflow.processEvent(event);
            
        } catch (Exception e) {
            // Error Handling Pattern: Log and suppress to prevent infinite retry loops 
            // (unless DLQ is configured)
            log.error("❌ Error processing event {}: {}", event.detectionId(), e.getMessage(), e);
        }
    }
}