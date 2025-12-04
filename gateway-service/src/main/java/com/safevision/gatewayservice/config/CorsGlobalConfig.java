package com.safevision.gatewayservice.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Global CORS configuration for the API Gateway.
 * Controls which domains (origins) and HTTP methods are allowed to access the backend API.
 */
@Slf4j
@Configuration
public class CorsGlobalConfig {

    /**
     * Configures the CORS source bean.
     * In development mode, we allow all origins ("*") to simplify frontend testing.
     *
     * @return The configured reactive CORS source.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        log.info("Initializing Global CORS Configuration...");

        CorsConfiguration config = new CorsConfiguration();

        // Allow any origin (e.g., http://localhost:4200, mobile apps, etc.)
        // Security Note: In production, replace "*" with specific domains.
        config.setAllowedOrigins(List.of("*"));

        // Define allowed HTTP methods
        config.setAllowedMethods(List.of(
            HttpMethod.GET.name(),
            HttpMethod.POST.name(),
            HttpMethod.PUT.name(),
            HttpMethod.PATCH.name(), // Required for updating resources partially
            HttpMethod.DELETE.name(),
            HttpMethod.OPTIONS.name() // Required for CORS preflight checks
        ));

        // Allow all headers (Content-Type, Authorization, etc.)
        config.setAllowedHeaders(List.of("*"));

        // Expose the Authorization header so the frontend can read the JWT
        config.setExposedHeaders(List.of("Authorization"));

        // Credentials (cookies) are disabled because allowedOrigins is "*"
        // If credentials are needed, specific origins must be defined.
        config.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Apply this configuration to all routes (/**)
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}