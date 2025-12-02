package com.safevision.authservice.service;

import com.safevision.authservice.model.User; // Importante: Importar seu Model
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {

    @Value("${safevision.jwt.secret}")
    private String secretKey;

    @Value("${safevision.jwt.expiration-ms:86400000}")
    private long expirationMs;

    private Key key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    // --- MUDANÇA PRINCIPAL AQUI ---
    // Agora aceita o objeto User completo para extrair ID e Roles
    public String generateToken(User user) {
        
        // Claims são os dados extras que vão dentro do token
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", user.getId());       // Adiciona o UUID
        claims.put("roles", user.getRoles()); // Adiciona a lista ["ADMIN", "USER"]

        return Jwts.builder()
                .setClaims(claims) // Injeta os dados extras
                .setSubject(user.getUsername()) // O "dono" do token
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
}