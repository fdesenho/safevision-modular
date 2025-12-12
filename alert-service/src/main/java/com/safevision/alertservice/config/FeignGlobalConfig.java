package com.safevision.alertservice.config;

import feign.RequestInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Global configuration for OpenFeign Clients.
 * <p>
 * This class is responsible for injecting cross-cutting concerns into
 * outgoing HTTP requests made by Feign Clients.
 * </p>
 * <p>
 * It specifically handles Service-to-Service authentication by injecting
 * a shared internal secret key into the request headers.
 * </p>
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(JwtProperties.class)
public class FeignGlobalConfig {

    private static final String INTERNAL_AUTH_HEADER = "X-Internal-Key";

    private final JwtProperties jwtProperties;

    /**
     * Creates a global RequestInterceptor for all Feign Clients.
     * <p>
     * This interceptor automatically appends the {@code X-Internal-Key} header
     * to every outgoing request, allowing the Auth Service to recognize
     * this service as a trusted internal component.
     * </p>
     *
     * @return The configured {@link RequestInterceptor}.
     */
    @Bean
    public RequestInterceptor internalKeyInterceptor() {
        log.info("Initializing Feign Internal Key Interceptor with header: {}", INTERNAL_AUTH_HEADER);

        return template -> {
            String secretKey = jwtProperties.secret();

            if (secretKey == null || secretKey.isBlank()) {
                log.warn("Internal JWT Secret is missing! Service-to-Service calls may fail with 401.");
            }

            // Using trace/debug level to avoid leaking sensitive data in production logs
            log.trace("Injecting internal authentication header for request to: {}", template.url());

            template.header(INTERNAL_AUTH_HEADER, secretKey);
        };
    }
}