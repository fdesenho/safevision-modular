package com.safevision.alertservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Type-safe configuration properties for JWT settings.
 * Maps properties starting with "safevision.jwt" from application.yml.
 *
 * @param secret The shared secret key used for HMAC signature.
 */
@ConfigurationProperties(prefix = "safevision.jwt")
public record JwtProperties(String secret) {}