//package com.safevision.recognitionservice.integration;
//
//import static org.awaitility.Awaitility.await;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.atLeastOnce;
//import static org.mockito.Mockito.verify;
//
//import java.math.BigDecimal;
//import java.util.concurrent.TimeUnit;
//
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.amqp.rabbit.core.RabbitTemplate;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.mock.mockito.MockBean;
//
//import com.safevision.recognitionservice.AbstractIntegrationTest;
//import com.safevision.recognitionservice.dto.RawTrackingEvent;
//import com.safevision.recognitionservice.dto.AlertEventDTO;
//import com.safevision.recognitionservice.producer.AlertProducer;
//
//class RecognitionIntegrationTest extends AbstractIntegrationTest {
//
//    @Autowired
//    private RabbitTemplate rabbitTemplate; // Template REAL para enviar o trigger
//
//    @MockBean
//    private AlertProducer alertProducer; // Mockamos apenas a SAÍDA da lógica
//
//    private final String INPUT_QUEUE = "vision.raw.tracking";
//
//    @Test
//    @DisplayName("Rule: Weapon Detection - Should trigger immediate alert")
//    void shouldTriggerWeaponAlert() {
//        var event = createEvent("det-1", true, false, 50);
//
//        // Envia de verdade para o RabbitMQ (Container)
//        rabbitTemplate.convertAndSend(INPUT_QUEUE, event);
//
//        // Verifica se a lógica de negócio CHAMOU o produtor de alertas
//        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
//            verify(alertProducer, atLeastOnce()).sendAlert(any(AlertEventDTO.class));
//        });
//    }
//
//    @Test
//    @DisplayName("Rule: Persistent Stare - Should trigger alert after threshold")
//    void shouldTriggerStareAlertAfterThreshold() {
//        String detId = "det-stare";
//        
//        // Envia 10 eventos (Limite definido no seu ThreatAnalysisService)
//        for (int i = 0; i < 10; i++) {
//            rabbitTemplate.convertAndSend(INPUT_QUEUE, createEvent(detId, false, true, 40));
//        }
//
//        await()
//        .atMost(15, TimeUnit.SECONDS) // Aumentamos um pouco o fôlego
//        .pollInterval(500, TimeUnit.MILLISECONDS)
//        .untilAsserted(() -> {
//            verify(alertProducer, atLeastOnce()).sendAlert(any(AlertEventDTO.class));
//        });
//    }
//
//    @Test
//    @DisplayName("Rule: Approaching - Should detect rapid depth change")
//    void shouldTriggerApproachingAlert() {
//        String detId = "det-approach";
//        
//        // Simula aproximação: depth subindo de 10 para 40 (diferença > 15)
//        int[] depths = {10, 12, 15, 20, 25, 30, 35, 40, 42, 45};
//
//        for (int d : depths) {
//            rabbitTemplate.convertAndSend(INPUT_QUEUE, createEvent(detId, false, false, d));
//        }
//
//        await()
//        .atMost(15, TimeUnit.SECONDS) // Aumentamos um pouco o fôlego
//        .pollInterval(500, TimeUnit.MILLISECONDS)
//        .untilAsserted(() -> {
//            verify(alertProducer, atLeastOnce()).sendAlert(any(AlertEventDTO.class));
//        });
//    }
//
//    // Helper para criar o Record com os 13 campos obrigatórios
//    private RawTrackingEvent createEvent(String id, boolean hasWeapon, boolean isFacing, int depth) {
//        return new RawTrackingEvent(
//            id, System.currentTimeMillis(), "CAM-01", "user-fábio",
//            isFacing, "CENTER", depth, hasWeapon, 
//            hasWeapon ? "PISTOLA" : null, hasWeapon ? "MAO" : null,
//            null, new BigDecimal("-27.0"), new BigDecimal("-48.0")
//        );
//    }
//}