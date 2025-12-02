package com.safevision.alertservice.listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.safevision.alertservice.dto.AlertEventDTO; // <--- Importamos o DTO oficial
import com.safevision.alertservice.service.AlertService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AlertListener {

	private final AlertService alertService;

	@RabbitListener(queues = "#{alertsQueueName}")
    public void receiveAlert(AlertEventDTO event) {
        System.out.println("📨 [RabbitMQ] Recebido alerta de: " + event.alertType());

        try {
            // CORREÇÃO: Chamamos o método novo que aceita o DTO direto
            alertService.createAlert(event);
            
        } catch (Exception e) {
            // Log de erro
            System.err.println("❌ Erro ao processar alerta do RabbitMQ: " + e.getMessage());
            
            // Dica Pro: Em produção, se você lançar a exceção aqui (throw e),
            // o RabbitMQ tentará reenviar a mensagem. Se engolir o erro (try-catch), a mensagem é perdida.
        }
    }
}