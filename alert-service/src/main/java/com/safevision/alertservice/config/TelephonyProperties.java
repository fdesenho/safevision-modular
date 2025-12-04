package com.safevision.alertservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration Record for Telephony settings (Twilio).
 * Replaces scattered @Value annotations with a type-safe object.
 */
@ConfigurationProperties(prefix = "telephony")
public record TelephonyProperties(
    String accountSid,
    String authToken,
    String fromNumber,
    String toNumber, // Fallback number
    String baseUrl
) {}