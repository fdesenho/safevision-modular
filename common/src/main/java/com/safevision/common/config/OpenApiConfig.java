package com.safevision.common.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração centralizada para documentação OpenAPI (Swagger).
 * Baseado na ADR-011: API Documentation.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI safeVisionOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SafeVision - Modular Ecosystem")
                        .description("Plataforma de monitoramento inteligente e reconhecimento. " +
                                     "Esta documentação segue os padrões estabelecidos na ADR-011.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Fabio Desenho - Software Architect")
                                .email("fdesenho@exemplo.com")
                                .url("https://github.com/fdesenho/safevision-modular"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://springdoc.org")))
                .externalDocs(new ExternalDocumentation()
                        .description("Repositório e Documentação de Arquitetura")
                        .url("https://github.com/fdesenho/safevision-modular"));
    }
}