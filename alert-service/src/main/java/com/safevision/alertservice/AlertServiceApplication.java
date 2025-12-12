package com.safevision.alertservice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@Slf4j
@SpringBootApplication
@EnableFeignClients(basePackages = "com.safevision.alertservice.client")
public class AlertServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AlertServiceApplication.class, args);
        log.info("🚀 Alert Service started successfully!");
    }

    /**
     * Bean para comunicação HTTP síncrona.
     * Utilizado pelo TelephonyService para conectar na Twilio.
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}