package com.safevision.gatewayservice.routes;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;

@Configuration
public class GatewayRoutes {

    private static final String V3_API_DOCS = "/v3/api-docs";

    @Value("${safevision.services.auth}")
    private String authServiceUrl;

    @Value("${safevision.services.alert}")
    private String alertServiceUrl;

    @Value("${safevision.services.recognition}")
    private String recognitionServiceUrl;

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()

                // ======================================
                // AUTH SERVICE
                // ======================================
                .route("auth-service", r -> r
                        .path("/auth/**") // Captura tudo que começa com /auth
                        .filters(f -> f
                                .circuitBreaker(c -> c
                                        .setName("authServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback")
                                )
                                // REMOVIDO: .stripPrefix(1) 
                                // Motivo: O AuthController já espera receber "/auth/..."
                        )
                        .uri(authServiceUrl)
                )

                // Rota do Swagger (Aqui o stripPrefix/rewritePath é necessário e está correto)
                .route("auth-service-swagger", r -> r
                        .path("/aggregate/auth-service" + V3_API_DOCS)
                        .filters(f -> f
                                .rewritePath("/aggregate/auth-service" + V3_API_DOCS, V3_API_DOCS)
                                .circuitBreaker(c -> c
                                        .setName("authServiceSwaggerCircuitBreaker")
                                        .setFallbackUri("forward:/fallback")
                                )
                        )
                        .uri(authServiceUrl)
                )


                // ======================================
                // ALERT SERVICE
                // ======================================
                .route("alert-service", r -> r
                        .path("/alert/**") // CORRIGIDO: De "/alert/**" para "/alerts/**" (Plural) para bater com SecurityConfig
                        .filters(f -> f
                                .circuitBreaker(c -> c
                                        .setName("alertServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback")
                                )
                                // REMOVIDO: .stripPrefix(1)
                        )
                        .uri(alertServiceUrl)
                )

                .route("alert-service-swagger", r -> r
                        .path("/aggregate/alert-service" + V3_API_DOCS)
                        .filters(f -> f
                                .rewritePath("/aggregate/alert-service" + V3_API_DOCS, V3_API_DOCS)
                                .circuitBreaker(c -> c
                                        .setName("alertServiceSwaggerCircuitBreaker")
                                        .setFallbackUri("forward:/fallback")
                                )
                        )
                        .uri(alertServiceUrl)
                )


                // ======================================
                // RECOGNITION SERVICE
                // ======================================
                .route("recognition-service", r -> r
                        .path("/recognition/**")
                        .filters(f -> f
                                .circuitBreaker(c -> c
                                        .setName("recognitionServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback")
                                )
                                // REMOVIDO: .stripPrefix(1)
                        )
                        .uri(recognitionServiceUrl)
                )

                .route("recognition-service-swagger", r -> r
                        .path("/aggregate/recognition-service" + V3_API_DOCS)
                        .filters(f -> f
                                .rewritePath("/aggregate/recognition-service" + V3_API_DOCS, V3_API_DOCS)
                                .circuitBreaker(c -> c
                                        .setName("recognitionServiceSwaggerCircuitBreaker")
                                        .setFallbackUri("forward:/fallback")
                                )
                        )
                        .uri(recognitionServiceUrl)
                )


                // ======================================
                // GLOBAL FALLBACK ROUTE
                // ======================================
                .route("fallbackRoute", r -> r
                        .path("/fallback")
                        .filters(f -> f.filter((exchange, chain) ->
                                Mono.just(exchange.getResponse())
                                        .flatMap(response -> {
                                            response.setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
                                            // Mensagem simples para o cliente saber que o serviço alvo caiu
                                            byte[] bytes = "{\"error\": \"Service Unavailable\", \"message\": \"O microsserviço de destino está offline ou demorando para responder.\"}"
                                                    .getBytes();
                                            response.getHeaders().add("Content-Type", "application/json");
                                            return response.writeWith(
                                                    Mono.just(response.bufferFactory().wrap(bytes))
                                            );
                                        })
                        ))
                        .uri("no://op")
                )

                .build();
    }
}