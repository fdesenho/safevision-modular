package com.safevision.recognitionservice.listener;

import com.safevision.recognitionservice.dto.RawTrackingEvent;
import com.safevision.recognitionservice.facade.TrackingWorkflowFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Infrastructure Layer: RabbitMQ Message Listener.
 * <p>
 * This component acts as the entry point for the Recognition Service.
 * It consumes high-frequency raw data from the Vision Agent and delegates
 * the processing to the Business Facade.
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VisionAgentListener {

    // Injection of the Facade, adhering to the Facade Pattern to decouple
    // infrastructure logic (RabbitMQ) from domain logic (Threat Analysis).
    private final TrackingWorkflowFacade trackingWorkflow;

    /**
     * Consumes raw tracking events from the Python Agent.
     * <p>
     * Uses SpEL (Spring Expression Language) to resolve the queue name dynamically
     * from the Bean 'rawTrackingQueueName' defined in RabbitMQConfig.
     * </p>
     *
     * @param event DTO containing raw tracking data (gaze, position, objects).
     */
    @RabbitListener(queues = "#{rawTrackingQueueName}")
    public void handleRawTrackingEvent(RawTrackingEvent event) {
        
        try {
            if (event == null) {
                log.warn("⚠️ Received null event from RabbitMQ. Skipping.");
                return;
            }

            // Use 'debug' for high-frequency events to avoid log flooding in production
            log.debug("🤖 [Recognition] Received raw event ID: {}", event.detectionId());
            
            // Delegate to the Facade (Business Logic)
            trackingWorkflow.processEvent(event);
            
        } catch (Exception e) {
            // Error Handling Pattern: Log and suppress.
            // We catch generic exceptions here to prevent the RabbitMQ listener container
            // from entering an infinite retry loop for "poison pill" messages.
            var eventId = (event != null) ? event.detectionId() : "unknown";
            log.error("❌ Error processing tracking event {}: {}", eventId, e.getMessage(), e);
        }
    }
}