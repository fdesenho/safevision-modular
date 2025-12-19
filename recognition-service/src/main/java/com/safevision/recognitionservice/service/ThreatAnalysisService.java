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
 * Core Logic Service (The "Brain" of Recognition).
 * <p>
 * Analyzes raw tracking telemetry received from the Vision Agent (Edge)
 * and applies security rules (Business Logic) to determine if a critical
 * alert should be triggered.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ThreatAnalysisService {

    private final AlertProducer alertProducer;
    private final MovementHistoryService historyService;

    // --- STATE VARIABLES ---
    
    // Rule 1: Stare Counter (Persistence of Vision)
    private final Map<String, Integer> stareCountMap = new ConcurrentHashMap<>();
    
    // ⚠️ AJUSTE: Python roda a 0.5s (2 FPS). 
    // Para 5 segundos de detecção: 5s / 0.5s = 10 frames.
    private static final int STARE_THRESHOLD = 10; 

    // Rule 2: Loitering Configurations
    // Janela de histórico ajustada para 5 segundos (10 frames)
    private static final int LOITERING_WINDOW = 10;
    private static final int PROXIMITY_DIFFERENCE = 15;

    // --- VISUAL MEMORY (EVIDENCE CACHE) ---
    // Stores the last valid snapshot URL for a detection ID.
    // Resolves the issue where some frames might detect a threat but fail to upload an image.
    private final Map<String, String> evidenceCache = new ConcurrentHashMap<>();

    /**
     * Main entry point for event analysis.
     * Orchestrates the execution of all security rules.
     *
     * @param event The raw tracking data from the Vision Agent.
     */
    public void analyze(RawTrackingEvent event) {
        if (event == null) return;
        
        String id = event.detectionId();
        log.trace("Analyzing event for ID: {}", event.detectionId());
        
        // 1. UPDATE EVIDENCE CACHE
        // If this packet contains a snapshot, save it as the "best evidence so far".
        if (event.snapshotUrl() != null && !event.snapshotUrl().isEmpty()) {
            evidenceCache.put(id, event.snapshotUrl());
            log.debug("📸 Evidence cached for ID: {}", id);
        }

        // 2. Rule: Weapon Detection (Highest Priority)
        analyzeWeaponRule(event);

        // 3. Rule: Persistent Stare (Behavioral)
        analyzeStareRule(event);

        // 4. Rule: Loitering / Approach (Behavioral)
        analyzeLoiteringRule(event);
        
        // Note: Cache cleanup should happen when object leaves the scene.
        // For MVP, we clean up when an alert is triggered or state resets.
    }

    // ==============================================================
    // RULE 0: WEAPON DETECTION
    // ==============================================================
    private void analyzeWeaponRule(RawTrackingEvent event) {
        if (event.hasWeapon()) {
            log.warn("🔫 WEAPON DETECTED! ID: {}", event.detectionId());
            sendCriticalAlert(
                event,
                event.weaponType() +"_DETECTADA",
                event.weaponType() + " foi localizada na " + event.weaponLocation()
            );
        }
    }

    // ==============================================================
    // RULE 1: STARE DETECTION
    // ==============================================================
    private void analyzeStareRule(RawTrackingEvent event) {
        String id = event.detectionId();

        if (event.isFacingCamera()) {
            // Incrementa o contador para este ID
            int currentCount = stareCountMap.merge(id, 1, Integer::sum);

            if (currentCount >= STARE_THRESHOLD) {
                log.warn("👁️ Persistent Stare Detected! ID: {}", id);
                
                // ⚠️ AJUSTE: Cálculo de tempo baseado no intervalo de 0.5s
                double seconds = STARE_THRESHOLD * 0.5; 
                
                sendCriticalAlert(
                    event,
                    "OBSERVACAO_DETECTADA",
                    "Pessoa te observou por " + seconds + " segundos."
                );

                // Reset and Cleanup
                stareCountMap.remove(id);
                evidenceCache.remove(id); // Consume evidence
            }
        } else {
            // Reset counter if eye contact is broken
            stareCountMap.remove(id);
        }
    }

    // ==============================================================
    // RULE 2: LOITERING / APPROACH
    // ==============================================================
    private void analyzeLoiteringRule(RawTrackingEvent event) {
        historyService.recordEvent(event);
        
        String id = event.detectionId();
        List<Integer> depths = historyService.getDepths(id);

        if (depths.size() < LOITERING_WINDOW) return;

        int currentDepth = depths.get(0); 
        int initialDepth = depths.get(LOITERING_WINDOW - 1); 
        boolean isApproaching = (currentDepth - initialDepth) >= PROXIMITY_DIFFERENCE;

        if (isApproaching) {
            log.warn("🚶 Threat Approaching! ID: {}", id);

            sendCriticalAlert(
                event,
                "PESSOA_APROXIMANDO",
                "Pessoa que está rondando e se aproximando rapidamente do perímetro de segurança."
            );

            historyService.clearHistory(id);
            evidenceCache.remove(id); 
        }
    }

    // ==============================================================
    // UNIFIED ALERT DISPATCHER
    // ==============================================================
    /**
     * Constructs the Alert DTO using the current event data and cached evidence,
     * then dispatches it to the Message Broker.
     */
    private void sendCriticalAlert(RawTrackingEvent event, String type, String description) {
        
        // 1. Try to get the snapshot from the current event
        String finalSnapshotUrl = event.snapshotUrl();

        // 2. If null, fallback to the Evidence Cache (best photo from previous frames)
        if (finalSnapshotUrl == null || finalSnapshotUrl.isEmpty()) {
            finalSnapshotUrl = evidenceCache.get(event.detectionId());
            if (finalSnapshotUrl != null) {
                log.info("📎 Attached cached evidence to alert: {}", finalSnapshotUrl);
            }
        }

        // 3. Create DTO with new GPS fields
        // Note: 'address' is null here because it's resolved asynchronously by the Alert Service later.
        AlertEventDTO finalAlert = new AlertEventDTO(
            event.userId(),
            type,
            description,
            "CRITICAL",
            event.cameraId(),
            finalSnapshotUrl, // Best available evidence
            event.latitude(), // 📍 GPS Latitude from Python
            event.longitude(),// 📍 GPS Longitude from Python
            null              // Address (to be resolved by Alert Service)
        );
        
        alertProducer.sendAlert(finalAlert);
    }
}