package com.safevision.authservice.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for RabbitMQ infrastructure in the Auth Service.
 * Sets up the necessary queues and message converters for asynchronous communication.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(RabbitQueueProperties.class)
public class RabbitMQConfig {

    private final RabbitQueueProperties queueProperties;

    /**
     * Exposes the Vision Configuration queue name as a String Bean.
     * This allows services (like UserService) to inject the queue name directly
     * without hardcoding strings.
     */
    @Bean
    public String visionConfigurationQueueName() {
        return queueProperties.visionConfiguration();
    }

    /**
     * Declares the physical Queue in RabbitMQ for Vision Configuration.
     * This queue is used to send dynamic updates (e.g., camera URL changes) to the Python Agent.
     */
    @Bean
    public Queue visionConfigQueue() {
        log.info("Configuring RabbitMQ Queue: {}", queueProperties.visionConfiguration());
        return new Queue(queueProperties.visionConfiguration(), true);
    }

    /**
     * Configures the Jackson converter to serialize messages as JSON.
     * Essential for interoperability with the Python Vision Agent.
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}