package com.safevision.alertservice.config;

import java.util.List;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Security configuration class for the Alert Service.
 * <p>
 * This configuration handles:
 * <ul>
 * <li>Stateless Session Management (JWT based)</li>
 * <li>CORS Configuration (Critical for WebSocket via Gateway)</li>
 * <li>Endpoint Authorization (Public vs Protected)</li>
 * </ul>
 * </p>
 */
@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Enables @PreAuthorize support at method level
@RequiredArgsConstructor
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {

    private final JwtProperties jwtProperties;

    /**
     * List of endpoints that bypass authentication.
     * Note: "/ws/**" is critical for the initial WebSocket Handshake.
     */
    private static final String[] PUBLIC_ENDPOINTS = {
        "/v3/api-docs/**",
        "/swagger-ui/**",
        "/swagger-ui.html",
        "/actuator/**", // Health checks
        "/ws/**",
        "/alert/ws/**"
    };

    /**
     * Configures the HTTP security filter chain.
     *
     * @param http The HttpSecurity builder.
     * @return The built SecurityFilterChain.
     * @throws Exception If an error occurs during configuration.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.info("🔒 Initializing Security Filter Chain for Alert Service...");

        http
            // 1. Disable CSRF (Not needed for stateless APIs)
            .csrf(AbstractHttpConfigurer::disable)
            
            // 2. Enable CORS (Cross-Origin Resource Sharing)
            // This is required because the Gateway/Frontend runs on a different port/origin logic
            .cors(AbstractHttpConfigurer::disable)
            
            // 3. Configure Stateless Session
            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // 4. Define Route Authorization
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(PUBLIC_ENDPOINTS).permitAll() // Allow Handshake
                .anyRequest().authenticated()                  // All other API calls need JWT
            )
            
            // 5. Configure OAuth2 Resource Server (JWT)
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        return http.build();
    }

    /**
     * Configures CORS settings to allow WebSocket connections from the Gateway/Frontend.
     * * @return The CORS configuration source.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Allow all origins (In production, replace "*" with specific Gateway URL)
        configuration.setAllowedOriginPatterns(List.of("*")); 
        
        // Allow standard HTTP methods
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        
        // Allow all headers (Authorization, Content-Type, etc.)
        configuration.setAllowedHeaders(List.of("*"));
        
        // Critical for WebSocket: Allow credentials (cookies/auth headers)
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Configures the JWT Decoder using the symmetric secret key.
     * * @return The JwtDecoder instance.
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        byte[] keyBytes = jwtProperties.secret().getBytes();
        SecretKey originalKey = new SecretKeySpec(keyBytes, 0, keyBytes.length, "HmacSHA256");
        
        return NimbusJwtDecoder.withSecretKey(originalKey).build();
    }
}