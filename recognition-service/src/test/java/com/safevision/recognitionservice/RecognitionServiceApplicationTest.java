package com.safevision.recognitionservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

@SpringBootTest(properties = {
    "spring.main.allow-bean-definition-overriding=true",
    "spring.rabbitmq.listener.auto-startup=false" // 👈 Não tenta ligar o motor do Rabbit
})
class RecognitionServiceApplicationTest {

    @MockBean private ConnectionFactory connectionFactory;
    @MockBean private RabbitAdmin rabbitAdmin;
    @MockBean private RabbitTemplate rabbitTemplate;

    @Test
    void contextLoads() {
        // Sucesso garantido sem precisar de conexão real
    }
    
}