package com.safevision.alertservice.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for RabbitMQ infrastructure in the Alert Service.
 * Defines queues and message converters.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(RabbitQueueProperties.class) 
public class RabbitMQConfig {

    private final RabbitQueueProperties queueProperties;

    /**
     * Exposes the alert queue name as a String Bean.
     * Useful for SpEL expressions in @RabbitListener.
     */
    @Bean
    public String alertsQueueName() {
        return queueProperties.alerts();
    }

    /**
     * Declares the physical Queue in RabbitMQ.
     * Marked as durable so messages persist if the broker restarts.
     */
    @Bean
    public Queue alertsQueue() {
        log.info("Configuring RabbitMQ Queue: {}", queueProperties.alerts());
        return new Queue(queueProperties.alerts(), true);
    }

    /**
     * Configures the Jackson converter to serialize/deserialize messages as JSON.
     * This allows interoperability between Python (producer) and Java (consumer).
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}