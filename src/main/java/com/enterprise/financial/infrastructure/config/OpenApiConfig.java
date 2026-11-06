package com.enterprise.financial.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";
    public static final String IDEMPOTENCY_HEADER = "X-Idempotency-Key";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Enterprise Financial Core API")
                        .version("1.0.0")
                        .description("Motor transacional de alta confiabilidade para processamento bancário e pagamentos, " +
                                "projetado com Clean Architecture, controle estrito de concorrência com travas pessimistas, " +
                                "garantias de idempotência e segurança stateless baseada em tokens JWT.")
                        .contact(new Contact()
                                .name("Viktor Gabriel")
                                .url("https://github.com/ViktorGabriel"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Insira o token JWT gerado no endpoint /api/v1/auth/login")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
    }

    @Bean
    public OperationCustomizer customizeIdempotencyHeader() {
        return (operation, handlerMethod) -> {
            String path = handlerMethod.getMethod().getDeclaringClass().getSimpleName();
            if (path.contains("TransactionController")) {
                HeaderParameter idempotencyHeader = new HeaderParameter();
                idempotencyHeader.setName(IDEMPOTENCY_HEADER);
                idempotencyHeader.setDescription("Chave única de idempotência para garantir execução atômica sem duplicação (ex: UUID v4)");
                idempotencyHeader.setRequired(true);
                idempotencyHeader.setSchema(new StringSchema().example("9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d"));

                operation.addParametersItem(idempotencyHeader);
            }
            return operation;
        };
    }
}
