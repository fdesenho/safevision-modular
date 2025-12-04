package com.safevision.authservice.config;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration class for the Auth Service.
 * Configures authentication mechanisms, password encoding, and JWT validation.
 */
@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {

    private final JwtProperties jwtProperties;

    // List of endpoints that do not require authentication
    private static final String[] PUBLIC_ENDPOINTS = {
        "/auth/login",
        "/auth/register",
        "/v3/api-docs/**",
        "/swagger-ui/**",
        "/swagger-ui.html"
    };

    /**
     * Configures the HTTP security filter chain.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.info("Initializing Security Filter Chain for Auth Service...");

        http
            // Disable CSRF as we use stateless JWTs
            .csrf(AbstractHttpConfigurer::disable)
            
            .authorizeHttpRequests(auth -> auth
                // 1. Allow public endpoints (Login, Register, Docs)
                .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                
                // 2. Block everything else
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
        byte[] keyBytes = jwtProperties.secret().getBytes();
        SecretKey originalKey = new SecretKeySpec(keyBytes, 0, keyBytes.length, "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(originalKey).build();
    }

    /**
     * Exposes the AuthenticationManager bean, required by the AuthController.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Configures the AuthenticationProvider with UserDetailsService and PasswordEncoder.
     */
    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    /**
     * Configures the PasswordEncoder (BCrypt).
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}