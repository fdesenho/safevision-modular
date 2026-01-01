package com.safevision.recognitionservice.producer;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import com.safevision.recognitionservice.dto.AlertEventDTO;

@ExtendWith(MockitoExtension.class)
class AlertProducerTest {

    @Mock private RabbitTemplate rabbitTemplate;
    
    // O construtor do seu AlertProducer pede a String alertsQueueName
    private final String queueName = "safevision.alerts";
    
    @Test
    void shouldSendAlertToRabbit() {
        AlertProducer producer = new AlertProducer(rabbitTemplate, queueName);
        var alert = new AlertEventDTO("u1", "TYPE", "DESC", "CRIT", "C1", null, null, null, null);

        producer.sendAlert(alert);

        verify(rabbitTemplate, times(1)).convertAndSend(eq(queueName), eq(alert));
    }
}