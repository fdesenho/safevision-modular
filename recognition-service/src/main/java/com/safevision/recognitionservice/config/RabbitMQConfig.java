package com.safevision.recognitionservice.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Infrastructure configuration for RabbitMQ.
 * Responsible for declaring queues and configuring the JSON message converter.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(RabbitQueueProperties.class) // Activates the Record
public class RabbitMQConfig {

    private final RabbitQueueProperties queueProperties;


    /**
     * Exposes the raw tracking queue name as a String Bean.
     * Bean Name: 'rawTrackingQueueName'
     */
    @Bean
    public String rawTrackingQueueName() {
        return queueProperties.rawTracking();
    }

    /**
     * Exposes the alerts queue name as a String Bean.
     * Bean Name: 'alertsQueueName'
     */
    @Bean
    public String alertsQueueName() {
        return queueProperties.alerts();
    }



    /**
     * Declares the Raw Tracking Queue (Input).
     */
    @Bean
    public Queue rawTrackingQueue() {
        log.info("Configuring Raw Input Queue: {}", queueProperties.rawTracking());
        return new Queue(queueProperties.rawTracking(), true);
    }

    /**
     * Declares the Alerts Queue (Output).
     */
    @Bean
    public Queue alertsQueue() {
        log.info("Configuring Alert Output Queue: {}", queueProperties.alerts());
        return new Queue(queueProperties.alerts(), true);
    }

    /**
     * Configures the Jackson converter to serialize/deserialize messages as JSON.
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}