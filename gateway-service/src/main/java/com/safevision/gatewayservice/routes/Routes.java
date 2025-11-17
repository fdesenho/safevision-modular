package com.safevision.gatewayservice.routes;

import static org.springframework.cloud.gateway.server.mvc.filter.FilterFunctions.setPath;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.server.mvc.filter.CircuitBreakerFilterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;




@Configuration
public class Routes {

	private static final String V3_API_DOCS = "/v3/api-docs";

	@Value("${safevision.services.auth}")
    private String authServiceUrl;

    @Value("${safevision.services.alert}")
    private String alertServiceUrl;

    @Value("${safevision.services.recognition}")
    private String recognitionServiceUrl;
    
    private static final String PATH_AUTH_SERVICE = "/auth/**";
    private static final String PATH_RECOGNITION_SERVICE = "/recognition/**";
    private static final String PATH_ALERT_SERVICE = "/alert/**";
    
    
    // IDs de Rotas
    public static final String ROUTE_ID_AUTH_SERVICE = "auth_service";
    public static final String ROUTE_ID_AUTH_SERVICE_SWAGGER = "auth_service_swagger";
    public static final String ROUTE_ID_ALERT_SERVICE = "alert_service";
    public static final String ROUTE_ID_ALERT_SERVICE_SWAGGER = "alert_service_swagger";
    public static final String ROUTE_ID_RECOGNITION_SERVICE = "recognition_service";
    public static final String ROUTE_ID_RECOGNITION_SERVICE_SWAGGER = "recognition_service_swagger";
    public static final String ROUTE_ID_FALLBACK = "fallbackRoute";

    // Nomes de Circuit Breakers
    public static final String CB_AUTH_SERVICE = "authServiceCircuitBreaker";
    public static final String CB_AUTH_SERVICE_SWAGGER = "authServiceSwaggerCircuitBreaker";
    public static final String CB_ALERT_SERVICE = "alertServiceCircuitBreaker";
    public static final String CB_ALERT_SERVICE_SWAGGER = "alertServiceSwaggerCircuitBreaker";
    public static final String CB_RECOGNITION_SERVICE = "recognitionServiceCircuitBreaker";
    public static final String CB_RECOGNITION_SERVICE_SWAGGER = "recognitionServiceSwaggerCircuitBreaker";

    // Caminhos de Fallback
    public static final URI FALLBACK_URI = URI.create("forward:/fallbackRoute");

    // Nomes de Serviço (para uso interno ou em path predicates se necessário)
    public static final String SERVICE_NAME_AUTH = "auth-service";
    public static final String SERVICE_NAME_ALERT = "alert-service";
    public static final String SERVICE_NAME_RECOGNITION = "recognition-service";

    // URLs para reescrita de Path (Swagger)
    public static final String PATH_SWAGGER_API_DOCS = V3_API_DOCS;


    // =============================
    // AUTH-SERVICE ROUTES
    // =============================

    @Bean
    public RouterFunction<ServerResponse> authServiceRoute() {
        return GatewayRouterFunctions.route(ROUTE_ID_AUTH_SERVICE) // Usando constante
                .route(RequestPredicates.path(PATH_AUTH_SERVICE), HandlerFunctions.http(authServiceUrl))
                .filter(CircuitBreakerFilterFunctions.circuitBreaker(
                        CB_AUTH_SERVICE, // Usando constante
                        FALLBACK_URI // Usando constante
                ))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> authServiceSwaggerRoute() {
        return GatewayRouterFunctions.route(ROUTE_ID_AUTH_SERVICE_SWAGGER) // Usando constante
                .route(RequestPredicates.path("/aggregate/" + SERVICE_NAME_AUTH + V3_API_DOCS), // Usando constante para nome do serviço
                        HandlerFunctions.http(authServiceUrl))
                .filter(CircuitBreakerFilterFunctions.circuitBreaker(
                        CB_AUTH_SERVICE_SWAGGER, // Usando constante
                        FALLBACK_URI)) // Usando constante
                .filter(setPath(PATH_SWAGGER_API_DOCS)) // Usando constante
                .build();
    }


    // =============================
    // ALERT-SERVICE ROUTES
    // =============================

    @Bean
    public RouterFunction<ServerResponse> alertServiceRoute() {
        return GatewayRouterFunctions.route(ROUTE_ID_ALERT_SERVICE) // Usando constante
                .route(RequestPredicates.path(PATH_ALERT_SERVICE), HandlerFunctions.http(alertServiceUrl))
                .filter(CircuitBreakerFilterFunctions.circuitBreaker(
                        CB_ALERT_SERVICE, // Usando constante
                        FALLBACK_URI // Usando constante
                ))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> alertServiceSwaggerRoute() {
        return GatewayRouterFunctions.route(ROUTE_ID_ALERT_SERVICE_SWAGGER) // Usando constante
                .route(RequestPredicates.path("/aggregate/" + SERVICE_NAME_ALERT + V3_API_DOCS), // Usando constante
                        HandlerFunctions.http(alertServiceUrl))
                .filter(CircuitBreakerFilterFunctions.circuitBreaker(
                        CB_ALERT_SERVICE_SWAGGER, // Usando constante
                        FALLBACK_URI)) // Usando constante
                .filter(setPath(PATH_SWAGGER_API_DOCS)) // Usando constante
                .build();
    }


    // =============================
    // RECOGNITION-SERVICE ROUTES
    // =============================

    @Bean
    public RouterFunction<ServerResponse> recognitionServiceRoute() {
        return GatewayRouterFunctions.route(ROUTE_ID_RECOGNITION_SERVICE) // Usando constante
                .route(RequestPredicates.path(PATH_RECOGNITION_SERVICE), HandlerFunctions.http(recognitionServiceUrl))
                .filter(CircuitBreakerFilterFunctions.circuitBreaker(
                        CB_RECOGNITION_SERVICE, // Usando constante
                        FALLBACK_URI // Usando constante
                ))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> recognitionServiceSwaggerRoute() {
        return GatewayRouterFunctions.route(ROUTE_ID_RECOGNITION_SERVICE_SWAGGER) // Usando constante
                .route(RequestPredicates.path("/aggregate/" + SERVICE_NAME_RECOGNITION + V3_API_DOCS), // Usando constante
                        HandlerFunctions.http(recognitionServiceUrl))
                .filter(CircuitBreakerFilterFunctions.circuitBreaker(
                        CB_RECOGNITION_SERVICE_SWAGGER, // Usando constante
                        FALLBACK_URI)) // Usando constante
                .filter(setPath(PATH_SWAGGER_API_DOCS)) // Usando constante
                .build();
    }
    
}


    // =================