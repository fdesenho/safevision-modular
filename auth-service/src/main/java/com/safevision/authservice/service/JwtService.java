package com.safevision.authservice.service;

import com.safevision.authservice.config.JwtProperties;
import com.safevision.authservice.model.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

/**
 * Service responsible for generating and signing JSON Web Tokens (JWT).
 * It uses a shared secret key to ensure tokens can be validated by other microservices.
 */
@Slf4j
@Service
@EnableConfigurationProperties(JwtProperties.class)
public class JwtService {

    private static final String CLAIM_ID = "id";
    private static final String CLAIM_ROLES = "roles";
    

    private final SecretKey key;
    private final long expirationMs;

    /**
     * Constructor injection ensures the key is initialized only once and is immutable.
     *
     * @param properties The type-safe configuration object.
     */
    public JwtService(JwtProperties properties) {
        this.expirationMs = properties.expirationMs();
        this.key = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generates a signed JWT for a specific user.
     * Includes custom claims (ID, Roles) to allow stateless authorization in other services.
     *
     * @param user The authenticated user model.
     * @return The compact JWT string.
     */
    public String generateToken(User user) {
        var now = System.currentTimeMillis();
        var validity = new Date(now + expirationMs);

        
        var claims = Map.of(
            CLAIM_ID, user.getId(),
            CLAIM_ROLES, user.getRoles()
            
        );

        log.debug("Generating JWT for user: {}", user.getUsername());

        return Jwts.builder()
                .claims(claims) 
                .subject(user.getUsername()) 
                .issuedAt(new Date(now))
                .expiration(validity)
                .signWith(key, Jwts.SIG.HS256) 
                .compact();
    }
}