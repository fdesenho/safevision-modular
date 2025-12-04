package com.safevision.gatewayservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Type-safe configuration properties for JWT settings in the Gateway.
 * Maps properties starting with "safevision.jwt".
 *
 * @param secret The shared HMAC secret key used to validate tokens.
 */
@ConfigurationProperties(prefix = "safevision.jwt")
public record JwtProperties(String secret) {}