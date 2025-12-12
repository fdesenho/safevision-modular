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

    // --- VARIÁVEIS DE ESTADO ---
    
    // Regra 1: Contador de Olhar
    private final Map<String, Integer> stareCountMap = new ConcurrentHashMap<>();
    private static final int STARE_THRESHOLD = 20; 

    // Regra 2: Configurações
    private static final int LOITERING_WINDOW = 20;
    private static final int PROXIMITY_DIFFERENCE = 15;

    // --- NOVO: CACHE DE EVIDÊNCIAS (MEMÓRIA VISUAL) ---
    // Armazena a última URL de foto válida recebida para cada ID de detecção.
    // Isso resolve o problema de alertas disparados em frames sem foto.
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
        // 1. ATUALIZAR CACHE DE EVIDÊNCIA
        // Se este pacote trouxe uma foto, salvamos ela como a "melhor foto até agora" para este ID.
        if (event.snapshotUrl() != null && !event.snapshotUrl().isEmpty()) {
            evidenceCache.put(id, event.snapshotUrl());
            log.debug("📸 Evidence cached for ID: {}", id);
        }

        // 2. Regra de Arma (Prioridade Máxima)
        analyzeWeaponRule(event);

        // 3. Regra de Olhar
        analyzeStareRule(event);

        // 4. Regra de Rondar
        analyzeLoiteringRule(event);
        
        // Nota: A limpeza do evidenceCache deve ocorrer quando o objeto sai de cena.
        // Como não temos um evento "saiu de cena" explícito do Python, 
        // podemos limpar quando o contador de Stare é resetado ou manter até expirar (via TTL), 
        // mas para este MVP, vamos limpar apenas ao disparar o alerta ou no reset do stare.
    }

    // ==============================================================
    // RULE 0: WEAPON
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
    // RULE 1: STARE
    // ==============================================================
    private void analyzeStareRule(RawTrackingEvent event) {
        String id = event.detectionId();

        if (event.isFacingCamera()) {
            int currentCount = stareCountMap.merge(id, 1, Integer::sum);

            if (currentCount >= STARE_THRESHOLD) {
                log.warn("👁️ Persistent Stare Detected! ID: {}", id);
                
                double seconds = STARE_THRESHOLD / 10.0; 
                sendCriticalAlert(
                    event,
                    "OBSERVACAO_DETECTADA",
                    "Pessoa te observou por " + seconds + " seconds."
                );

                // Reset e Limpeza
                stareCountMap.remove(id);
                evidenceCache.remove(id); // Limpa a foto usada
            }
        } else {
            // Se parou de olhar, reseta o contador
            stareCountMap.remove(id);
            // Opcional: Limpar a foto também se quiser que a "sessão de foto" seja vinculada ao olhar
            // evidenceCache.remove(id); 
        }
    }

    // ==============================================================
    // RULE 2: LOITERING
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
                "LOITERING_AND_APPROACH",
                "Person loitering and rapidly approaching the camera."
            );

            historyService.clearHistory(id);
            evidenceCache.remove(id); // Limpa a foto usada
        }
    }

    // ==============================================================
    // MÉTODO UNIFICADO DE ENVIO (COM LÓGICA DE RECUPERAÇÃO DE FOTO)
    // ==============================================================
    private void sendCriticalAlert(RawTrackingEvent event, String type, String description) {
        
        // 1. Tenta pegar a URL que veio neste evento específico
        String finalSnapshotUrl = event.snapshotUrl();

        // 2. Se veio nula, tenta pegar do CACHE (a melhor foto recente)
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
            finalSnapshotUrl // Envia a melhor URL encontrada
        );
        
        alertProducer.sendAlert(finalAlert);
    }
}