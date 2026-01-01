//package com.safevision.alertservice.service;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.Mockito.when;
//
//import java.util.Collections;
//
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import com.safevision.alertservice.client.UserPreferenceClient;
//import com.safevision.alertservice.repository.AlertRepository;
//import com.safevision.alertservice.service.AlertService;
//import com.safevision.alertservice.service.GeocodingService;
//
//@ExtendWith(MockitoExtension.class)
//class AlertServiceLogicTest {
//
//    @Mock private AlertRepository alertRepository;
//    @Mock private UserPreferenceClient preferenceClient;
//    @Mock private GeocodingService geocodingService;
//    
//    @InjectMocks private AlertService alertService;
//
//    @Test
//    void processAlert_ShouldHandleEmptyPreferences() {
//        // Testa o branch onde o usuário não tem preferências cadastradas
//        when(preferenceClient.getUserPreferences(anyString())).thenReturn(Collections.emptyList());
//        
//        // Chame o método de processamento do seu service
//        // alertService.processIncomingAlert(mockAlertEvent);
//        
//        // Verifique se ele salvou no banco mas não tentou enviar notificações
//    }
//
//    @Test
//    void processAlert_ShouldHandleGeocodingFailure() {
//        // Testa o branch onde o serviço de mapas falha
//        when(geocodingService.reverseGeocode(any(), any())).thenThrow(new RuntimeException("Map API Down"));
//        
//        // Valida se o sistema continua rodando (resiliência)
//    }
//}