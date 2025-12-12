package com.safevision.alertservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Type-safe configuration properties for RabbitMQ queues.
 * Maps properties starting with "rabbitmq.queues" from application.yml.
 *
 * @param alerts The name of the alerts queue.
 */
@ConfigurationProperties(prefix = "spring.rabbitmq.queues")
public record RabbitQueueProperties(String alerts) {}