package com.safevision.authservice.dto;

import java.util.Set;

/**
 * DTO para retornar dados de usuário de forma segura (sem o hash da senha).
 */
public record UserResponse(
    String id,
    String username,
    Set<String> roles
) {}