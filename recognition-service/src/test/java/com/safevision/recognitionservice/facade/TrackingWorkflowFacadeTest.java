package com.safevision.recognitionservice.facade;

import com.safevision.recognitionservice.dto.RawTrackingEvent;
import com.safevision.recognitionservice.service.MovementHistoryService;
import com.safevision.recognitionservice.service.ThreatAnalysisService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrackingWorkflowFacadeTest {

    @Mock private ThreatAnalysisService threatAnalysisService;
    @Mock private MovementHistoryService movementHistoryService;
    @InjectMocks private TrackingWorkflowFacade facade;

    @Test
    void shouldExecuteWorkflowInOrder() {
        var event = new RawTrackingEvent("d1", 123L, "c1", "u1", false, null, 50, false, null, null, null, BigDecimal.ZERO, BigDecimal.ZERO);
        
        facade.processEvent(event);

        // Verifica se chamou o histórico E a análise
        verify(movementHistoryService).recordEvent(event);
        verify(threatAnalysisService).analyze(event);
    }

    @Test
    void shouldIgnoreNullEvent() {
        facade.processEvent(null);
        verifyNoInteractions(movementHistoryService, threatAnalysisService);
    }
}