package com.safevision.alertservice.config;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration class for the Alert Service.
 * Configures JWT validation, CORS, and endpoint protection.
 */
@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Enables @PreAuthorize support
@RequiredArgsConstructor
@EnableConfigurationProperties(JwtProperties.class) // Enables the record
public class SecurityConfig {

    private final JwtProperties jwtProperties;

    // List of endpoints that do not require authentication
    private static final String[] PUBLIC_ENDPOINTS = {
        "/v3/api-docs/**",
        "/swagger-ui/**",
        "/swagger-ui.html",
        "/ws/**",
        "/alert/ws/**"
    };

    /**
     * Configures the HTTP security filter chain.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.info("Initializing Security Filter Chain for Alert Service...");

        http
            // Disable CSRF as we use stateless JWTs
            .csrf(AbstractHttpConfigurer::disable) 
            
            .authorizeHttpRequests(auth -> auth
                // 1. Allow public endpoints (Swagger, WebSocket handshake)
                .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                
                // 2. Block everything else. 
                // This ensures internal calls (e.g., /alerts/event) must carry a valid JWT.
                .anyRequest().authenticated()
            )
            
            // Ensure stateless session management (no JSESSIONID)
            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // Configure Resource Server to accept JWTs
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        return http.build();
    }

    /**
     * Configures the JWT Decoder using the shared secret key.
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        // Uses the secret from the type-safe record
        byte[] keyBytes = jwtProperties.secret().getBytes();
        SecretKey originalKey = new SecretKeySpec(keyBytes, 0, keyBytes.length, "HmacSHA256");
        
        return NimbusJwtDecoder.withSecretKey(originalKey).build();
    }
}