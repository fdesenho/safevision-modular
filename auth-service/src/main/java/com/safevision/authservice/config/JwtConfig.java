package com.safevision.authservice.config;

import java.util.Base64;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

// --- Nimbus JOSE + JWT Imports ---
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

@Configuration
public class JwtConfig {

    @Value("${jwt.secret-base64}")
    private String base64Secret;

    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        // 1. Decode the base64 secret string into a raw byte array
        byte[] keyBytes = Base64.getDecoder().decode(base64Secret);
        
        // 2. Create the standard Java SecretKey
        SecretKey secretKey = new SecretKeySpec(keyBytes, "HmacSHA256");

        // 3. Convert the Java SecretKey into a Nimbus OctetSequenceKey (JWK format for symmetric keys)
        // This is the crucial step to bridge the Java crypto and Nimbus library types.
        JWK jwk = new OctetSequenceKey.Builder(secretKey)
                .keyID("auth-key") // Optional: provides an ID for the key
                .build();
        
        // 4. Create the JWKSource, which holds the set of available keys
        // ImmutableJWKSet is the standard implementation of JWKSource
        return new ImmutableJWKSet<>(new JWKSet(jwk));
    }

    @Bean
    public JwtEncoder jwtEncoder(JWKSource<SecurityContext> jwkSource) {
        // The NimbusJwtEncoder uses the JWKSource to find the key for signing tokens.
        return new NimbusJwtEncoder(jwkSource);
    }
}