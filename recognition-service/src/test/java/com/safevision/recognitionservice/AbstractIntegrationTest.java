package com.safevision.recognitionservice;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.main.allow-bean-definition-overriding=true",
        "spring.rabbitmq.listener.simple.auto-startup=true",
        "spring.rabbitmq.listener.simple.missing-queues-fatal=false",
        // Dá tempo ao RabbitMQ para respirar no handshake
        "spring.rabbitmq.connection-timeout=10000"
    }
)
@Testcontainers
public abstract class AbstractIntegrationTest {

    @Container
    @ServiceConnection
    static RabbitMQContainer rabbitMQContainer = new RabbitMQContainer("rabbitmq:3.12-management")
            .waitingFor(Wait.forLogMessage(".*Server startup complete.*", 1)
            .withStartupTimeout(Duration.ofMinutes(2)));

    @Configuration
    static class TestRabbitConfig {

        // 1. Força a criação das filas assim que a conexão estabilizar
        @Bean
        public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
            return new RabbitAdmin(connectionFactory);
        }

        @Bean(name = "rawTrackingQueueName")
        @Primary
        public String rawTrackingQueueName() {
            return "safevision.vision.raw.tracking";
        }

        @Bean(name = "alertsQueueName")
        @Primary
        public String alertsQueueName() {
            return "safevision.alerts";
        }

        @Bean
        public Queue rawTrackingQueue() {
            return new Queue("safevision.vision.raw.tracking", true, false, false);
        }

        @Bean
        public Queue alertsQueue() {
            return new Queue("safevision.alerts", true, false, false);
        }
    }
}