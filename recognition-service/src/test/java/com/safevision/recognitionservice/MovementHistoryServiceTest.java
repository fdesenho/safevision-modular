package com.safevision.recognitionservice;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.safevision.recognitionservice.dto.RawTrackingEvent;
import com.safevision.recognitionservice.service.MovementHistoryService;

class MovementHistoryServiceTest {

    private MovementHistoryService historyService;

    @BeforeEach
    void setUp() {
        historyService = new MovementHistoryService();
    }

    @Test
    @DisplayName("Deve manter apenas os últimos 10 eventos (Janela Deslizante)")
    void recordEvent_SlidingWindow_Success() {
        String detId = "test-1";
        
        // Adiciona 15 eventos com profundidades de 1 a 15
        for (int i = 1; i <= 15; i++) {
            historyService.recordEvent(createMockEvent(detId, i));
        }

        List<Integer> depths = historyService.getDepths(detId);

        // 1. O tamanho deve ser exatamente 10 (conforme HISTORY_WINDOW_SIZE)
        assertThat(depths).hasSize(10);
        
        // 2. Como usamos history.addFirst(), o primeiro elemento deve ser o último inserido (15)
        assertThat(depths.get(0)).isEqualTo(15);
        
        // 3. O último da lista deve ser o 6 (pois o 1-5 foram removidos da janela)
        assertThat(depths.get(9)).isEqualTo(6);
    }

    @Test
    @DisplayName("Deve retornar lista vazia para ID inexistente")
    void getDepths_EmptyForUnknownId() {
        List<Integer> depths = historyService.getDepths("unknown");
        assertThat(depths).isEmpty();
    }

    @Test
    @DisplayName("Deve remover o histórico ao limpar")
    void clearHistory_Success() {
        String detId = "to-clear";
        historyService.recordEvent(createMockEvent(detId, 50));
        
        assertThat(historyService.getDepths(detId)).isNotEmpty();
        
        historyService.clearHistory(detId);
        
        assertThat(historyService.getDepths(detId)).isEmpty();
    }

    private RawTrackingEvent createMockEvent(String id, int depth) {
        return new RawTrackingEvent(id, System.currentTimeMillis(), "CAM", "USER", 
                                   false, null, depth, false, null, null, null, 
                                   BigDecimal.ZERO, BigDecimal.ZERO);
    }
}