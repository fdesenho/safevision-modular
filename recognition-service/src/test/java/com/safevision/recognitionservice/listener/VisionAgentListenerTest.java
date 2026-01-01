package com.safevision.recognitionservice.listener;

import com.safevision.recognitionservice.dto.RawTrackingEvent;
import com.safevision.recognitionservice.facade.TrackingWorkflowFacade;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VisionAgentListenerTest {

    @Mock private TrackingWorkflowFacade facade;
    @InjectMocks private VisionAgentListener listener;

    @Test
    void shouldHandleEventSuccessfully() {
        var event = new RawTrackingEvent("det-1", 123L, "cam-1", "user-1", true, "CENTER", 50, false, null, null, null, BigDecimal.ZERO, BigDecimal.ZERO);
        
        listener.handleRawTrackingEvent(event);

        verify(facade, times(1)).processEvent(event);
    }

    @Test
    void shouldHandleNullEventSilently() {
        listener.handleRawTrackingEvent(null);
        verifyNoInteractions(facade);
    }
    
    @Test
    void shouldHandleExceptionInListener() {
        var event = new RawTrackingEvent("det-1", 123L, "c1", "u1", false, null, 50, false, null, null, null, BigDecimal.ZERO, BigDecimal.ZERO);
        
        // Simula um erro na facade para cair no catch do listener
        doThrow(new RuntimeException("Simulated Error")).when(facade).processEvent(event);
        
        // Não deve lançar exceção para fora, pois o listener tem try-catch
        listener.handleRawTrackingEvent(event);
        
        verify(facade).processEvent(event);
    }
}