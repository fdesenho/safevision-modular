package com.safevision.recognitionservice.service;

import com.safevision.recognitionservice.dto.RawTrackingEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service responsible for managing the recent historical state (tracking data)
 * for each detected object in memory.
 * <p>
 * This acts as an in-memory 'Repository' or 'Cache' for time-series analysis
 * required by the ThreatAnalysisService. It stores a sliding window of depth/position data.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MovementHistoryService {

   
    private final Map<String, LinkedList<Integer>> historyMap = new ConcurrentHashMap<>();
    
   
    private static final int HISTORY_WINDOW_SIZE = 10;

    /**
     * Records the new depth measurement for the tracked object.
     * Maintains a fixed-size sliding window by evicting old data.
     *
     * @param event The raw tracking data event.
     */
    public void recordEvent(RawTrackingEvent event) {
        var id = event.detectionId();
        
   
        var history = historyMap.computeIfAbsent(id, k -> new LinkedList<>());
        
   
        history.addFirst(event.depthPosition());
        
   
        while (history.size() > HISTORY_WINDOW_SIZE) {
            history.removeLast();
        }
    }

    /**
     * Retrieves the list of recent depth measurements for the tracked object.
     * Returns an empty list if no history is found (Null Object Pattern).
     *
     * @param detectionId The ID of the tracked object.
     * @return A list of the last N depth values.
     */
    public List<Integer> getDepths(String detectionId) {
        return historyMap.getOrDefault(detectionId, new LinkedList<>());
    }
    
    /**
     * Clears the tracking history for a specific object.
     * Typically called after a CRITICAL alert is fired to reset the analysis state.
     *
     * @param detectionId The ID of the object whose history must be cleared.
     */
    public void clearHistory(String detectionId) {
        if (historyMap.containsKey(detectionId)) {
            log.debug("Clearing tracking history for ID: {}", detectionId);
            historyMap.remove(detectionId);
        }
    }
}