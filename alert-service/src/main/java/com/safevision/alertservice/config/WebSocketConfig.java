package com.safevision.alertservice.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configuration class for WebSocket using STOMP protocol.
 * Enables real-time communication between the backend and the frontend (Angular).
 */
@Slf4j
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private static final String BROKER_PREFIX = "/topic";
    private static final String APP_PREFIX = "/app";
    private static final String ENDPOINT_WS = "/ws";
    private static final String ENDPOINT_ALERT = "alert/ws";

    /**
     * Configures the message broker options.
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        log.info("Configuring WebSocket Message Broker...");
        config.enableSimpleBroker(BROKER_PREFIX);
        config.setApplicationDestinationPrefixes(APP_PREFIX);
    }

    /**
     * Registers the STOMP endpoints mapping each to a specific URL.
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        log.info("Registering STOMP Endpoint at: {}", ENDPOINT_WS);
        registry.addEndpoint(ENDPOINT_ALERT)
        .setAllowedOriginPatterns("*") 
        .withSockJS(); 
        registry.addEndpoint(ENDPOINT_WS)
        .setAllowedOriginPatterns("*") 
        .withSockJS(); 
    }
}