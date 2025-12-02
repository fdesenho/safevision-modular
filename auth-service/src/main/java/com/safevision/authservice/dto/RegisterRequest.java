package com.safevision.authservice.dto;

import java.util.Set;

/**
 * DTO para receber dados de registro de novos usuários.
 * O uso de 'record' (Java 16+) cria automaticamente getters, equals, hashCode e toString.
 */
public record RegisterRequest(
    String username,
    String password,
    Set<String> roles // Opcional: Permite criar ADMINs se necessário (cuidado em produção!)
) {}