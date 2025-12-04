package com.safevision.authservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Type-safe configuration properties for JWT settings.
 * Maps properties starting with "safevision.jwt" from application.yml.
 *
 * @param secret       The shared HMAC secret key.
 * @param expirationMs The token validity duration in milliseconds.
 */
@ConfigurationProperties(prefix = "safevision.jwt")
public record JwtProperties(String secret, long expirationMs) {}