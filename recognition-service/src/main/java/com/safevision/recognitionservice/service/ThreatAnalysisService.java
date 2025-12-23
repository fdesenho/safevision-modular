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

   
    private final Map<String, Integer> stareCountMap = new ConcurrentHashMap<>();
    
   
    private static final int STARE_THRESHOLD = 10; 

   
    private static final int LOITERING_WINDOW = 10;
    private static final int PROXIMITY_DIFFERENCE = 15;

   
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
        
       
        if (event.snapshotUrl() != null && !event.snapshotUrl().isEmpty()) {
            evidenceCache.put(id, event.snapshotUrl());
            log.debug("📸 Evidence cached for ID: {}", id);
        }

       
        analyzeWeaponRule(event);

       
        analyzeStareRule(event);

       
        analyzeLoiteringRule(event);
        
       
    }

   
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

   
    private void analyzeStareRule(RawTrackingEvent event) {
        String id = event.detectionId();

        if (event.isFacingCamera()) {
           
            int currentCount = stareCountMap.merge(id, 1, Integer::sum);

            if (currentCount >= STARE_THRESHOLD) {
                log.warn("👁️ Persistent Stare Detected! ID: {}", id);
                
           
                double seconds = STARE_THRESHOLD * 0.5; 
                
                sendCriticalAlert(
                    event,
                    "OBSERVACAO_DETECTADA",
                    "Pessoa te observou por " + seconds + " segundos."
                );

           
                stareCountMap.remove(id);
                evidenceCache.remove(id); 
            }
        } else {

            stareCountMap.remove(id);
        }
    }

  
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

  
    /**
     * Constructs the Alert DTO using the current event data and cached evidence,
     * then dispatches it to the Message Broker.
     */
    private void sendCriticalAlert(RawTrackingEvent event, String type, String description) {
        
        
        String finalSnapshotUrl = event.snapshotUrl();

        
        if (finalSnapshotUrl == null || finalSnapshotUrl.isEmpty()) {
            finalSnapshotUrl = evidenceCache.get(event.detectionId());
            if (finalSnapshotUrl != null) {
                log.info("📎 Attached cached evidence to alert: {}", finalSnapshotUrl);
            }
        }

        AlertEventDTO finalAlert = new AlertEventDTO(
            event.userId(),
            type,
            description,
            "CRITICAL",
            event.cameraId(),
            finalSnapshotUrl, 
            event.latitude(), 
            event.longitude(),
            null              
        );
        
        alertProducer.sendAlert(finalAlert);
    }
}