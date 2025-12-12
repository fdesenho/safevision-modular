package com.safevision.gatewayservice.config;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * Security Configuration for the API Gateway (Reactive).
 * <p>
 * This class acts as the first line of defense. It validates JWT tokens
 * for all incoming requests before routing them to microservices.
 * </p>
 */
@Slf4j
@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {

    private final JwtProperties jwtProperties;

    // List of public endpoints (Whitelist)
    private static final String[] PUBLIC_ENDPOINTS = {
        "/swagger-ui.html",
        "/swagger-ui/**",
        "/v3/api-docs/**",
        "/swagger-resources/**",
        "/auth/login",
        "/auth/register",
        "/alert/event", // Allow internal/machine event push
        "/alert/ws/**",
        "/actuator/**", // Health checks
        "/ws/**",
        "/alert/ws/**"
    };

    /**
     * Configures the Reactive Security Filter Chain.
     */
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        log.info("Initializing Reactive Security Filter Chain for Gateway...");

        http
            // Enable CORS (Cross-Origin Resource Sharing) using global config
            .cors(Customizer.withDefaults())
            
            // Disable CSRF (Stateful protection not needed for stateless REST APIs)
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            
            .authorizeExchange(exchanges -> exchanges
                // 1. Allow whitelisted endpoints
                .pathMatchers(PUBLIC_ENDPOINTS).permitAll()
                
                // 2. Require authentication for everything else
                .anyExchange().authenticated()
            )
            
            // Validate JWT Tokens (Resource Server)
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        return http.build();
    }

    /**
     * Configures the Reactive JWT Decoder.
     * Uses the shared secret key to verify the HMAC signature of incoming tokens.
     */
    @Bean
    public ReactiveJwtDecoder jwtDecoder() {
        byte[] keyBytes = jwtProperties.secret().getBytes();
        SecretKey originalKey = new SecretKeySpec(keyBytes, 0, keyBytes.length, MacAlgorithm.HS256.getName());
        
        return NimbusReactiveJwtDecoder.withSecretKey(originalKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }
}