package com.safevision.alertservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.email")
public record EmailProperties(
    String fromAddress,
    boolean enabled
) {}