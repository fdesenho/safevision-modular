package com.safevision.gatewayservice.config;

import org.springdoc.core.utils.Constants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {

    // Prefix for routing calls to auth-service via Gateway
    private static final String AUTH_SERVICE_PREFIX = "/auth-service";
    // Prefix for routing calls to auth-service via Gateway
    private static final String AUTH_LOGIN_PATH = "/auth/login";
    private static final String AUTH_REGISTER_PATH = "/auth/register";
    private static final String ALERT_EVENT_PATH = "/alerts/event";

    // Prometheus endpoint constant (Actuator)
    private static final String PROMETHEUS_ENDPOINT = "/actuator/prometheus";

    // Aggregated allowed public endpoints using constants instead of raw strings
    private static final String[] PUBLIC_ENDPOINTS = {
            Constants.SWAGGER_UI_URL,                // "/swagger-ui.html"
            Constants.SWAGGER_UI_PATH,               // "/swagger-ui/**"
            Constants.API_DOCS_URL,                  // "/v3/api-docs/**"
            Constants.SWAGGER_CONFIG_URL,            // "/swagger-resources/**"
            PROMETHEUS_ENDPOINT,
            AUTH_LOGIN_PATH,
            AUTH_REGISTER_PATH,
            ALERT_EVENT_PATH
            
    };

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchange -> exchange
                        .pathMatchers(PUBLIC_ENDPOINTS).permitAll()
                        .anyExchange().authenticated()
                )
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(List.of("*")); // Replace with frontend URL later
        config.setAllowedMethods(List.of(
                HttpMethod.GET.name(),
                HttpMethod.POST.name(),
                HttpMethod.PUT.name(),
                HttpMethod.DELETE.name(),
                HttpMethod.OPTIONS.name()
        ));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
