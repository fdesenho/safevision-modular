package com.safevision.gatewayservice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Entry point for the API Gateway Service.
 * <p>
 * This microservice acts as the single entry point (Edge Server) for all external traffic.
 * Its responsibilities include:
 * <ul>
 * <li><b>Routing:</b> Forwarding requests to Auth, Alert, or Recognition services based on the URL path.</li>
 * <li><b>Security:</b> Validating JWT Tokens before requests reach the internal services.</li>
 * <li><b>Resilience:</b> Implementing Circuit Breakers to prevent cascading failures.</li>
 * <li><b>Load Balancing:</b> Distributing traffic via Eureka Service Discovery.</li>
 * </ul>
 * </p>
 */
@Slf4j
@SpringBootApplication
@EnableConfigurationProperties // Enables support for Type-Safe Configuration (Records)
public class GatewayServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayServiceApplication.class, args);
        log.info("🚀 API Gateway started successfully on Java 21!");
    }
}