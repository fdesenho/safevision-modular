package com.safevision.authservice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Entry point for the Authentication Service.
 * <p>
 * This microservice acts as the <b>Identity Provider (IdP)</b> for the SafeVision architecture.
 * It is responsible for:
 * <ul>
 * <li>User Registration and Management (PostgreSQL).</li>
 * <li>Authentication (Login).</li>
 * <li>Generating JWT Tokens signed with the shared secret.</li>
 * </ul>
 * </p>
 */
@Slf4j
@SpringBootApplication
@EnableConfigurationProperties // Enables support for @ConfigurationProperties records
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
        log.info("🚀 Auth Service (Identity Provider) started successfully!");
    }
}