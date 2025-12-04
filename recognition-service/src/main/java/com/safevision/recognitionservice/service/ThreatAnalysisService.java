package com.safevision.recognitionservice.service;

import com.safevision.recognitionservice.dto.AlertEventDTO;
import com.safevision.recognitionservice.dto.RawTrackingEvent;
import com.safevision.recognitionservice.producer.AlertProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Core Logic Service (The "Brain").
 * Analyzes raw tracking events from the Vision Agent and applies security rules
 * to determine if a critical alert should be triggered.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ThreatAnalysisService {

    private final AlertProducer alertProducer;
    private final MovementHistoryService historyService;

    // --- RULE 1: PERSISTENT STARE ---
    // Stores stare counters per detection ID
    private final Map<String, Integer> stareCountMap = new ConcurrentHashMap<>();
    
    // Threshold: 50 events (~5 seconds at 10fps)
    private static final int STARE_THRESHOLD = 50; 

    // --- RULE 2: LOITERING AND APPROACHING ---
    // Analyze a window of 20 events (~2 seconds)
    private static final int LOITERING_WINDOW = 20;
    
    // If depth increases by 15 units within the window, it's a rapid approach
    private static final int PROXIMITY_DIFFERENCE = 15;


    /**
     * Main entry point for event analysis.
     * Orchestrates the execution of all security rules.
     *
     * @param event The raw tracking data from the Vision Agent.
     */
    public void analyze(RawTrackingEvent event) {
        if (event == null) return;

        log.trace("Analyzing event for ID: {}", event.detectionId());

        // 1. Check for Weapons (Zero Tolerance - Highest Priority)
        analyzeWeaponRule(event);

        // 2. Check for Persistent Staring
        analyzeStareRule(event);

        // 3. Check for Loitering and Approaching (Requires history)
        analyzeLoiteringRule(event);
    }

    // ==============================================================
    // RULE 0: WEAPON DETECTED
    // ==============================================================
    private void analyzeWeaponRule(RawTrackingEvent event) {
        if (event.hasWeapon()) {
            log.warn("🔫 WEAPON DETECTED! ID: {}", event.detectionId());
            
            sendCriticalAlert(
                event.userId(),
                event.cameraId(),
                "WEAPON_DETECTED",
                "IMMEDIATE DANGER: " + event.weaponType() + " detected at " + event.weaponLocation(),
                event.snapshotUrl()
            );
        }
    }

    // ==============================================================
    // RULE 1: PERSISTENT STARE
    // ==============================================================
    private void analyzeStareRule(RawTrackingEvent event) {
        var id = event.detectionId();

        if (event.isFacingCamera()) {
            // Increment counter if facing camera
            int currentCount = stareCountMap.merge(id, 1, Integer::sum);

            if (currentCount >= STARE_THRESHOLD) {
                log.warn("👁️ Persistent Stare Detected! ID: {}", id);
                
                double seconds = STARE_THRESHOLD / 10.0; // Assuming ~10 events/sec
                sendCriticalAlert(
                    event.userId(), 
                    event.cameraId(), 
                    "THREAT_STARE",
                    "Persistent Stare detected for approx " + seconds + " seconds.",
                    event.snapshotUrl()
                );

                // Reset counter to avoid flooding alerts
                stareCountMap.remove(id);
            }
        } else {
            // Reset counter if the person looks away
            stareCountMap.remove(id);
        }
    }

    // ==============================================================
    // RULE 2: LOITERING AND APPROACHING
    // ==============================================================
    private void analyzeLoiteringRule(RawTrackingEvent event) {
        // Record history first
        historyService.recordEvent(event);
        
        var id = event.detectionId();
        List<Integer> depths = historyService.getDepths(id);

        // Ensure we have enough history to analyze
        if (depths.size() < LOITERING_WINDOW) {
            return;
        }

        int currentDepth = depths.get(0); // Most recent depth
        int initialDepth = depths.get(LOITERING_WINDOW - 1); // Oldest depth in window

        // CRITERIA: Has depth increased significantly (approaching)?
        boolean isApproaching = (currentDepth - initialDepth) >= PROXIMITY_DIFFERENCE;

        if (isApproaching) {
            log.warn("🚶 Threat Approaching! ID: {}", id);

            sendCriticalAlert(
                event.userId(), 
                event.cameraId(), 
                "LOITERING_AND_APPROACH",
                "Person loitering and rapidly approaching the camera.",
                event.snapshotUrl()
            );

            // Clear history after triggering to prevent duplicate alerts for the same approach action
            historyService.clearHistory(id);
        }
    }

    // ==============================================================
    // INTERNAL: UNIFIED SEND METHOD
    // ==============================================================
    private void sendCriticalAlert(String userId, String cameraId, String type, String description, String snapshotUrl) {
        var finalAlert = new AlertEventDTO(
            userId,
            type,
            description,
            "CRITICAL",
            cameraId,
            snapshotUrl
        );
        
        // Delegate to producer to send via RabbitMQ
        alertProducer.sendAlert(finalAlert);
    }
}