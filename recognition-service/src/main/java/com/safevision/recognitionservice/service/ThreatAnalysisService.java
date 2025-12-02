package com.safevision.recognitionservice.service;

import com.safevision.recognitionservice.dto.AlertEventDTO;
import com.safevision.recognitionservice.dto.RawTrackingEvent;
import com.safevision.recognitionservice.producer.AlertProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class ThreatAnalysisService {

    private final AlertProducer alertProducer;
    private final MovementHistoryService historyService;

    // --- RULE 1 VARIABLES: PERSISTENT STARE ---
    private final Map<String, Integer> stareCountMap = new ConcurrentHashMap<>();
    // 20 events at ~0.1s interval = 2 seconds of staring
    private static final int TIME_MILI_SECONDS_STARE_THRESHOLD = 20;

    // --- RULE 2 VARIABLES: LOITERING AND APPROACHING ---
    // Analyze a window of 20 events (approx 2 seconds)
    private static final int LOITERING_WINDOW = 20;
    // If face size increases by 15 units within the window, it's a rapid approach
    private static final int PROXIMITY_DIFFERENCE = 15;


    /**
     * Main entry point for event analysis.
     * Orchestrates the execution of all security rules.
     * @param event The raw tracking data from the Vision Agent.
     */
    public void analyze(RawTrackingEvent event) {
        System.out.println("Analyzing event for ID: " + event.detectionId());

        // 1. Check for Weapons (Zero Tolerance)
        analyzeWeaponRule(event);

        // 2. Check for Persistent Staring
        analyzeStareRule(event);

        // 3. Check for Loitering and Approaching
        analyzeLoiteringRule(event);
    }

    // ==============================================================
    // RULE 0: WEAPON DETECTED
    // ==============================================================
    private void analyzeWeaponRule(RawTrackingEvent event) {
        if (event.hasWeapon()) {
            sendCriticalAlert(
                event.userId(),
                event.cameraId(),
                "WEAPON_DETECTED",
                "IMMEDIATE DANGER: " + event.weaponType() + " detected at " + event.weaponLocation()
            );
        }
    }

    // ==============================================================
    // RULE 1: PERSISTENT STARE
    // ==============================================================
    private void analyzeStareRule(RawTrackingEvent event) {
        String id = event.detectionId();

        if (event.isFacingCamera()) {
            // Increment counter if facing camera
            int currentCount = stareCountMap.merge(id, 1, Integer::sum);

            if (currentCount >= TIME_MILI_SECONDS_STARE_THRESHOLD) {
                // Trigger Alert
                double seconds = TIME_MILI_SECONDS_STARE_THRESHOLD / 10.0; // Assuming ~10 events/sec
                sendCriticalAlert(
                    event.userId(), 
                    event.cameraId(), 
                    "THREAT_STARE",
                    "Persistent Stare detected for approx " + seconds + " seconds."
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
    	historyService.recordEvent(event);
    	String id = event.detectionId();
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
            sendCriticalAlert(
                event.userId(), 
                event.cameraId(), 
                "LOITERING_AND_APPROACH",
                "Person loitering and rapidly approaching the camera."
            );

            // Clear history after triggering to prevent duplicate alerts for the same approach action
            historyService.clearHistory(id);
        }
    }

    // ==============================================================
    // INTERNAL: UNIFIED SEND METHOD
    // ==============================================================
    private void sendCriticalAlert(String userId, String cameraId, String type, String description) {
        AlertEventDTO finalAlert = new AlertEventDTO(
            userId,
            type,
            description,
            "CRITICAL",
            cameraId
        );
        // Delegate to producer to send via RabbitMQ
        alertProducer.sendAlert(finalAlert);
    }
}