package com.safevision.gatewayservice.routes;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.GatewayFilterSpec;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.cloud.gateway.route.builder.UriSpec;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import reactor.core.publisher.Mono;

import java.util.function.Function;

/**
 * Configuration class for API Gateway Routes.
 * <p>
 * Defines the routing logic, filters (Circuit Breaker), and fallback mechanisms
 * for all downstream microservices.
 * </p>
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class GatewayRoutes {

    // --- CONSTANTS ---
    private static final String V3_API_DOCS = "/v3/api-docs";
    private static final String FALLBACK_URI = "forward:/fallback";
    
    // Java 21 Text Block for readable JSON
    private static final String FALLBACK_JSON = """
        {
            "error": "Service Unavailable",
            "message": "The downstream microservice is offline or taking too long to respond. Please try again later."
        }
        """;

    // --- INJECTED SERVICE URLS ---
    @Value("${safevision.services.auth}")
    private String authServiceUrl;

    @Value("${safevision.services.alert}")
    private String alertServiceUrl;

    @Value("${safevision.services.recognition}")
    private String recognitionServiceUrl;

    /**
     * Defines the route locator with specific predicates and filters.
     */
    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        log.info("Configuring Gateway Routes...");

        return builder.routes()

                // ======================================
                // AUTH SERVICE
                // ======================================
                .route("auth-service", r -> r
                        .path("/auth/**")
                        .filters(this.applyCircuitBreaker("authServiceCircuitBreaker"))
                        .uri(authServiceUrl)
                )
                .route("auth-service-swagger", r -> r
                        .path("/aggregate/auth-service" + V3_API_DOCS)
                        .filters(f -> applySwaggerRewrite(f, "authServiceSwaggerCircuitBreaker"))
                        .uri(authServiceUrl)
                )

                // ======================================
                // ALERT SERVICE
                // ======================================
                .route("alert-service", r -> r
                        .path("/alert/**") // Singular path
                        .filters(this.applyCircuitBreaker("alertServiceCircuitBreaker"))
                        .uri(alertServiceUrl)
                )
                .route("alert-websocket", r -> r
                        .path("/alert/ws/**") // WebSocket Handshake
                        .filters(f -> f.rewritePath("/alert/ws/(?<segment>.*)", "/ws/${segment}"))
                        .uri(alertServiceUrl)
                )
                .route("alert-service-swagger", r -> r
                        .path("/aggregate/alert-service" + V3_API_DOCS)
                        .filters(f -> applySwaggerRewrite(f, "alertServiceSwaggerCircuitBreaker"))
                        .uri(alertServiceUrl)
                )

                // ======================================
                // RECOGNITION SERVICE
                // ======================================
                .route("recognition-service", r -> r
                        .path("/recognition/**")
                        .filters(this.applyCircuitBreaker("recognitionServiceCircuitBreaker"))
                        .uri(recognitionServiceUrl)
                )
                .route("recognition-service-swagger", r -> r
                        .path("/aggregate/recognition-service" + V3_API_DOCS)
                        .filters(f -> applySwaggerRewrite(f, "recognitionServiceSwaggerCircuitBreaker"))
                        .uri(recognitionServiceUrl)
                )

                // ======================================
                // GLOBAL FALLBACK ROUTE
                // ======================================
                .route("fallbackRoute", r -> r
                        .path("/fallback")
                        .filters(f -> f.filter((exchange, chain) -> {
                            var response = exchange.getResponse();
                            response.setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
                            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                            
                            return response.writeWith(
                                Mono.just(response.bufferFactory().wrap(FALLBACK_JSON.getBytes()))
                            );
                        }))
                        .uri("no://op")
                )

                .build();
    }

    // --- HELPER METHODS (Clean Code / DRY) ---

    /**
     * Applies the standard Circuit Breaker filter configuration.
     *
     * @param name The name of the circuit breaker instance.
     * @return A function that applies the filter.
     */
    private Function<GatewayFilterSpec, UriSpec> applyCircuitBreaker(String name) {
        return f -> f.circuitBreaker(c -> c
                .setName(name)
                .setFallbackUri(FALLBACK_URI)
        );
    }

    /**
     * Applies both Swagger path rewriting and Circuit Breaker.
     */
    private UriSpec applySwaggerRewrite(GatewayFilterSpec f, String cbName) {
        return f.rewritePath("/aggregate/[^/]+" + V3_API_DOCS, V3_API_DOCS)
                .circuitBreaker(c -> c
                        .setName(cbName)
                        .setFallbackUri(FALLBACK_URI)
                );
    }
}