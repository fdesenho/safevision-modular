package com.safevision.alertservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "telegram")
public record TelegramProperties(
    String botToken, 
    String chatId,   
    boolean enabled  
) {}