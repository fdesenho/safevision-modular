package com.safevision.gatewayservice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import jakarta.annotation.PostConstruct;

@SpringBootApplication
public class GatewayServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(GatewayServiceApplication.class, args);
      
    }
    @Value("${EUREKA_CLIENT_SERVICEURL_DEFAULTZONE:http://eureka-server:8761/eureka/}")
    String eurekaUrl;

    // Add a @PostConstruct method to confirm what URL is being used at runtime
    @PostConstruct
    public void logEurekaUrl() {
        System.out.println("Eureka URL being used: " + eurekaUrl);
        // You should see http://eureka-server:8761/eureka/ here.
    }
}
