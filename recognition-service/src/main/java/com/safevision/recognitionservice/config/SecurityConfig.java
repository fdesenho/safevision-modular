package com.safevision.recognitionservice.config;

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
 * Security configuration class for the Recognition Service.
 * Configures JWT validation, CORS, and endpoint protection.
 */
@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity 
@RequiredArgsConstructor
@EnableConfigurationProperties(JwtProperties.class) // Enables the record
public class SecurityConfig {

    private final JwtProperties jwtProperties;

    
    private static final String[] PUBLIC_ENDPOINTS = {
        "/v3/api-docs/**",
        "/swagger-ui/**",
        "/swagger-ui.html"
    };

    /**
     * Configures the HTTP security filter chain.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.info("Initializing Security Filter Chain for Recognition Service...");

        http
           
            .csrf(AbstractHttpConfigurer::disable)
            
            .authorizeHttpRequests(auth -> auth
           
                .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                
           
                .anyRequest().authenticated()
            )
            
           
            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
           
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        return http.build();
    }

    /**
     * Configures the JWT Decoder using the shared secret key.
     */
    @Bean
    public JwtDecoder jwtDecoder() {

        byte[] keyBytes = jwtProperties.secret().getBytes();
        SecretKey originalKey = new SecretKeySpec(keyBytes, 0, keyBytes.length, "HmacSHA256");
        
        return NimbusJwtDecoder.withSecretKey(originalKey).build();
    }
}