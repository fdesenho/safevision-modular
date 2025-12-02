package com.safevision.alertservice.config;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Permite usar @PreAuthorize se precisar no futuro
public class SecurityConfig {

    // Lê a chave injetada pelo Docker Compose
    @Value("${safevision.jwt.secret}")
    private String secretKey;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // API Stateless não precisa de CSRF
            .authorizeHttpRequests(auth -> auth
                // 1. Libera Documentação (Swagger)
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                
                // 2. IMPORTANTE: Bloqueia TODO o resto.
                // Isso inclui /alerts/event. O recognition-service TERÁ que enviar um Token JWT.
                .anyRequest().authenticated()
            )
            // Define que não guardamos sessão (Cookies) no servidor
            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // Ativa o Resource Server para validar o Token JWT
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        // Configura o decodificador para usar a mesma chave (HMAC) do Auth-Service
        byte[] keyBytes = secretKey.getBytes();
        SecretKey originalKey = new SecretKeySpec(keyBytes, 0, keyBytes.length, "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(originalKey).build();
    }
}