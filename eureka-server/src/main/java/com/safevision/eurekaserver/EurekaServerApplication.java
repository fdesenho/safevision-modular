package com.safevision.eurekaserver;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Entry point for the Eureka Service Discovery Server.
 * <p>
 * This server acts as the central registry ("Phone Book") for the microservices architecture.
 * All services (Auth, Alert, Recognition, Gateway) register here so they can find each other
 * dynamically without hardcoded IP addresses.
 * </p>
 */
@Slf4j
@EnableEurekaServer
@SpringBootApplication
public class EurekaServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
        log.info("🚀 Eureka Service Registry started successfully!");
    }
}