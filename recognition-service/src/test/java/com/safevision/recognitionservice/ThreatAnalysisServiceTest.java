package com.safevision.recognitionservice;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.safevision.recognitionservice.dto.RawTrackingEvent;
import com.safevision.recognitionservice.producer.AlertProducer;
import com.safevision.recognitionservice.service.MovementHistoryService;
import com.safevision.recognitionservice.service.ThreatAnalysisService;

@ExtendWith(MockitoExtension.class)
class ThreatAnalysisServiceTest {

    @Mock private AlertProducer alertProducer;
    @Mock private MovementHistoryService historyService;
    @InjectMocks private ThreatAnalysisService threatAnalysisService;

    @Test
    @DisplayName("Regra 1: Deve disparar alerta quando arma for detectada")
    void analyze_WeaponRule_Success() {
        var event = createEvent("det-1", true, false, 50);
        threatAnalysisService.analyze(event);
        verify(alertProducer, times(1)).sendAlert(any());
    }

    @Test
    @DisplayName("Regra 2: Deve disparar alerta após 10 encaradas (stare)")
    void analyze_StareRule_Success() {
        var event = createEvent("det-stare", false, true, 50);
        // Simula 10 chamadas
        for (int i = 0; i < 10; i++) {
            threatAnalysisService.analyze(event);
        }
        verify(alertProducer, atLeastOnce()).sendAlert(any());
    }

    @Test
    @DisplayName("Regra 3: Deve disparar alerta quando houver aproximação rápida (Janela de 10)")
    void analyze_LoiteringRule_Success() {
        String detId = "det-loiter";
        var event = createEvent(detId, false, false, 40);

        // Criamos uma lista com 10 elementos (o tamanho da janela)
        // index 0 (atual) = 40
        // index 9 (antigo) = 10
        // Diferença: 40 - 10 = 30 (que é >= 15)
        List<Integer> mockDepths = List.of(40, 35, 30, 25, 20, 18, 15, 12, 11, 10);
        
        when(historyService.getDepths(detId)).thenReturn(mockDepths);

        threatAnalysisService.analyze(event);

        // Agora sim o alertProducer deve ser chamado!
        verify(alertProducer, times(1)).sendAlert(any());
    }

    private RawTrackingEvent createEvent(String id, boolean weapon, boolean facing, int depth) {
        return new RawTrackingEvent(id, System.currentTimeMillis(), "C1", "U1", 
                                   facing, null, depth, weapon, "PISTOLA", "MAO", null, 
                                   BigDecimal.ZERO, BigDecimal.ZERO);
    }
}