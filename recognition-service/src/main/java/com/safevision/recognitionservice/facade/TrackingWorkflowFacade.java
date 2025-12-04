package com.safevision.recognitionservice.facade;

import com.safevision.recognitionservice.dto.RawTrackingEvent;
import com.safevision.recognitionservice.service.MovementHistoryService;
import com.safevision.recognitionservice.service.ThreatAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Facade Pattern: Orchestrates the threat detection pipeline.
 * <p>
 * This class decouples the message listener (Infrastructure Layer) from the
 * business logic (Domain Layer). It ensures that raw events go through the
 * correct sequence of processing steps: History Recording -> Threat Analysis.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TrackingWorkflowFacade {

    private final ThreatAnalysisService threatAnalysisService;
    private final MovementHistoryService movementHistoryService;

    /**
     * Orchestrates the processing of a single raw tracking event.
     *
     * @param event The raw telemetry data received from the Vision Agent.
     */
    public void processEvent(RawTrackingEvent event) {
        if (event == null) {
            log.warn("⚠️ Received null event in workflow. Skipping.");
            return;
        }

        log.debug("🔄 Starting workflow for Detection ID: {}", event.detectionId());

        // Step 1: Record state history (Required for time-based rules like Loitering)
        movementHistoryService.recordEvent(event);

        // Step 2: Execute the Threat Analysis Engine (The "Brain")
        // This checks all rules (Weapon, Stare, Loitering) and triggers alerts if necessary.
        threatAnalysisService.analyze(event);
        
        log.trace("✅ Workflow completed for Detection ID: {}", event.detectionId());
    }
}