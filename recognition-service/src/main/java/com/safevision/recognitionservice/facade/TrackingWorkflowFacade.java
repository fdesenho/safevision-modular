package com.safevision.recognitionservice.facade;

import com.safevision.recognitionservice.dto.RawTrackingEvent;
import com.safevision.recognitionservice.service.MovementHistoryService;
import com.safevision.recognitionservice.service.ThreatAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Facade Pattern: Encapsulates the complexity of the tracking workflow.
 * This ensures the Listener doesn't need to know the sequence of business logic steps.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TrackingWorkflowFacade {

    private final ThreatAnalysisService threatAnalysisService;
    private final MovementHistoryService movementHistoryService;

    /**
     * Orchestrates the processing of a raw tracking event.
     * 1. Records the event in history.
     * 2. Triggers threat analysis.
     *
     * @param event The raw data received from the agent.
     */
    public void processEvent(RawTrackingEvent event) {
        log.debug("Processing workflow for detection ID: {}", event.detectionId());

        // Step 1: Record state (History)
        movementHistoryService.recordEvent(event);

        // Step 2: Analyze logic (Brain)
        threatAnalysisService.analyze(event);
    }
}