package com.safevision.recognitionservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Type-safe configuration properties for RabbitMQ queues.
 * Maps properties starting with "rabbitmq.queues" from application.yml.
 * <p>
 * Utilizes Java 21 Records for immutability and concise syntax.
 * </p>
 *
 * @param rawTracking The name of the input queue (from Vision Agent).
 * @param alerts      The name of the output queue (to Alert Service).
 */
@ConfigurationProperties(prefix = "spring.rabbitmq.queues")
public record RabbitQueueProperties(String rawTracking, String alerts) {}