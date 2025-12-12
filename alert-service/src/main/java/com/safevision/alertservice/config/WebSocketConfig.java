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

        // Enables a simple in-memory broker to send messages to clients.
        // Prefix for messages going FROM server TO client (Push).
        config.enableSimpleBroker(BROKER_PREFIX);
        
        // Prefix for messages coming FROM client TO server (e.g. chat messages).
        config.setApplicationDestinationPrefixes(APP_PREFIX);
    }

    /**
     * Registers the STOMP endpoints mapping each to a specific URL.
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        log.info("Registering STOMP Endpoint at: {}", ENDPOINT_WS);
        
        registry.addEndpoint(ENDPOINT_ALERT)
        .setAllowedOriginPatterns("*") // Allows connections from any origin (CORS)
        .withSockJS(); // Fallback options for older browsers
        
        
        
        // Defines the connection endpoint (Handshake).
        // Angular will connect to: http://localhost:8080/alert/ws (via Gateway)
        registry.addEndpoint(ENDPOINT_WS)
                .setAllowedOriginPatterns("*") // Allows connections from any origin (CORS)
                .withSockJS(); // Fallback options for older browsers
    }
}