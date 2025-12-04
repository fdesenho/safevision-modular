package com.safevision.authservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Type-safe configuration properties for RabbitMQ queues in the Auth Service.
 * Maps properties starting with "rabbitmq.queues" from application.yml.
 *
 * @param visionConfiguration The name of the queue used to send configuration updates to the Vision Agent.
 */
@ConfigurationProperties(prefix = "rabbitmq.queues")
public record RabbitQueueProperties(String visionConfiguration) {}