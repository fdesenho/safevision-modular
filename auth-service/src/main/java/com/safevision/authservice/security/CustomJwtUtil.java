package com.safevision.authservice.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Date;

@Component
public class CustomJwtUtil {

    private final String jwtSecretBase64;
    private final long expirationMs;
    private Key key;

    public CustomJwtUtil(@Value("${jwt.secret-base64}") String jwtSecretBase64,
                   @Value("${jwt.expiration-ms}") long expirationMs) {
        this.jwtSecretBase64 = jwtSecretBase64;
        this.expirationMs = expirationMs;
    }

    @PostConstruct
    public void init() {
        byte[] decoded = Base64.getDecoder().decode(jwtSecretBase64);
        this.key = Keys.hmacShaKeyFor(decoded);
    }

    public String generateToken(String userId) {
        return Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
}
