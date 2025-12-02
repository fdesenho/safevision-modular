package com.safevision.alertservice.controller;

import com.safevision.alertservice.dto.AlertEventDTO;
import com.safevision.alertservice.dto.AlertResponse;
import com.safevision.alertservice.service.AlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/alert") // Singular (Alinhado com GatewayRoutes)
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    // -------------------------
    // Endpoint para Máquinas (Testes HTTP ou Fallback)
    // O fluxo principal de produção é via RabbitMQ
    // -------------------------
    @PostMapping("/event")
    public ResponseEntity<Void> receiveEvent(@RequestBody AlertEventDTO event) {
        
        // Validações básicas de entrada
        if (event == null || event.userId() == null || event.alertType() == null) {
            return ResponseEntity.badRequest().build();
        }

        // Chama o serviço (que agora sabe lidar com DTO)
        alertService.createAlert(event);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // -------------------------
    // Endpoints para Usuários (Frontend)
    // -------------------------

    @GetMapping
    public ResponseEntity<List<AlertResponse>> getMyAlerts(
            Authentication authentication,
            @RequestParam(defaultValue = "false") boolean unreadOnly) {
        
        // Extrai o usuário do Token JWT validado pelo SecurityConfig
        String userId = authentication.getName();
        
        // Busca usando a lógica do Service que já retorna DTOs
        List<AlertResponse> alerts = alertService.getUserAlerts(userId, unreadOnly);
        
        return ResponseEntity.ok(alerts);
    }

    @PatchMapping("/{id}/ack") // Patch é semanticamente correto para atualização parcial
    public ResponseEntity<Void> acknowledgeAlert(@PathVariable String id, Authentication authentication) {
        String userId = authentication.getName();
        
        boolean success = alertService.acknowledgeAlert(id, userId);
        
        if (success) {
            return ResponseEntity.noContent().build(); // 204 No Content (Sucesso sem corpo)
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // 404 (Não achou ou não é dono)
        }
    }
}