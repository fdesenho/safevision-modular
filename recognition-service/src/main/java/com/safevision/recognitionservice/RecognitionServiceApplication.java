package com.safevision.recognitionservice;

import com.safevision.recognitionservice.config.RabbitQueueProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Entry point for the Recognition Service (The "Brain" of the architecture).
 * <p>
 * This microservice is responsible for:
 * <ul>
 * <li>Consuming raw tracking data from the Vision Agent (Python) via RabbitMQ.</li>
 * <li>Analyzing behavioral patterns (e.g., Persistent Stare, Loitering).</li>
 * <li>Filtering noise and producing confirmed Critical Alerts to the Alert Service.</li>
 * </ul>
 * </p>
 */
@Slf4j
@SpringBootApplication
public class RecognitionServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RecognitionServiceApplication.class, args);
        log.info("🚀 Recognition Service (Analysis Engine) started successfully on Java 21!");
    }
}