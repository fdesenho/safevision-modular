package com.safevision.recognitionservice.controller;

import com.safevision.recognitionservice.producer.AlertProducer;
import com.safevision.recognitionservice.dto.AlertEventDTO; // Importe o DTO necessário
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller used for external access. The /simulate endpoint is used 
 * to manually test the health of the RabbitMQ producer and the Alert Service.
 */
@RestController
@RequestMapping("/recognition")
@RequiredArgsConstructor
public class RecognitionController {

    // Injeção do produtor de mensagens (RabbitMQ)
    private final AlertProducer alertProducer;

    /**
     * Simulates a detection event and sends it directly to the Alert Service queue.
     * This endpoint is protected by JWT.
     * @return Confirmation message.
     */
    @PostMapping("/simulate")
    public ResponseEntity<String> simulateDetection() {
        
        // 1. Cria um DTO com dados de teste CRÍTICO
        AlertEventDTO testAlert = new AlertEventDTO(
            "superadmin",
            "MANUAL_TRIGGER", // Tipo de Alerta para facilitar a identificação
            "Manual API test triggered via HTTP endpoint.",
            "CRITICAL",
            "CAM-TEST-00"
        );

        // 2. Dispara o alerta final (RabbitMQ)
        alertProducer.sendAlert(testAlert);

        return ResponseEntity.ok("Manual simulation successfully sent to Alert queue.");
    }
}