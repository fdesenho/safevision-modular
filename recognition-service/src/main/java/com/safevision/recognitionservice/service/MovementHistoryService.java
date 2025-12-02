package com.safevision.recognitionservice.service;

import com.safevision.recognitionservice.dto.RawTrackingEvent;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor; 

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service responsible for managing the recent historical state (tracking data) 
 * for each detected object in memory. 
 * This acts as an in-memory 'Repository' for time-series analysis required by ThreatAnalysisService.
 */
@Service
@RequiredArgsConstructor // Automatically creates a constructor for final fields
public class MovementHistoryService {

    // Final to ensure the map is not replaced, and ConcurrentHashMap for thread safety.
    private final Map<String, LinkedList<Integer>> historyMap = new ConcurrentHashMap<>();
    
    // Constant defining the size of the history window to analyze.
    private static final int HISTORY_SIZE = 10; // Analyze the last 10 events

    /**
     * Records the new depth measurement for the tracked object.
     * Ensures the history list maintains a fixed size (sliding window).
     * @param event The raw tracking data event.
     */
    public void recordEvent(RawTrackingEvent event) {
        String id = event.detectionId();
        
        // Gets the list or creates a new LinkedList if the key is absent.
        LinkedList<Integer> history = historyMap.computeIfAbsent(id, k -> new LinkedList<>());
        
        // Adds the newest depth at the start (index 0)
        history.addFirst(event.depthPosition());
        
        // Limits the history size (removes the oldest element)
        while (history.size() > HISTORY_SIZE) {
            history.removeLast();
        }
    }

    /**
     * Retrieves the list of recent depth measurements for the tracked object.
     * Returns an empty list if no history is found (never returns null).
     * @param detectionId The ID of the tracked object.
     * @return A list of the last N depth values.
     */
    public List<Integer> getDepths(String detectionId) {
        // Uses getOrDefault to guarantee a safe List object is always returned.
        return historyMap.getOrDefault(detectionId, new LinkedList<>());
    }
    
    /**
     * Clears the tracking history for a specific object, usually after a 
     * CRITICAL alert is fired (to prevent continuous flooding of alerts).
     * @param detectionId The ID of the object whose history must be cleared.
     */
    public void clearHistory(String detectionId) {
        historyMap.remove(detectionId);
    }
}