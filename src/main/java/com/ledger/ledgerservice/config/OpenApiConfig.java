package com.ledger.ledgerservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI ledgerOpenApi() {
        final String bearerSchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("Ledger Service API")
                        .description("REST APIs for Ledger Service: OTP authentication, user management, authorization, audit logging, and Excel processing.")
                        .version("v1")
                        .contact(new Contact().name("Ledger Service Team")))
                .addSecurityItem(new SecurityRequirement().addList(bearerSchemeName))
                .schemaRequirement(bearerSchemeName, new SecurityScheme()
                        .name(bearerSchemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT"));
    }
}
