package com.safevision.authservice.dto;

import java.util.Set;

public record RegisterRequest(
    String username,
    String password,
    String email,        // Novo
    String phoneNumber,  // Novo
    String cameraUrl,    // Novo
    Set<String> roles
) {}