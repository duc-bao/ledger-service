package com.ledger.ledgerservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${swagger.server-url:}")
    private String swaggerServerUrl;

    @Bean
    public OpenAPI ledgerOpenApi() {
        final String bearerSchemeName = "bearerAuth";

        List<Server> servers = new ArrayList<>();
        if (StringUtils.hasText(swaggerServerUrl) && !"/".equals(swaggerServerUrl.trim())) {
            servers.add(new Server().url(swaggerServerUrl.trim()).description("Configured Server URL"));
        }
        // Always provide "/" (Relative URL) so that Swagger UI executes API calls
        // relative to the current domain loaded in the browser (e.g. ledger.com.vn)
        servers.add(new Server().url("/").description("Current Host (Relative URL)"));

        return new OpenAPI()
                .info(new Info()
                        .title("Ledger Service API")
                        .description("REST APIs for Ledger Service: OTP authentication, user management, authorization, audit logging, and Excel processing.")
                        .version("v1")
                        .contact(new Contact().name("Ledger Service Team")))
                .servers(servers)
                .addSecurityItem(new SecurityRequirement().addList(bearerSchemeName))
                .schemaRequirement(bearerSchemeName, new SecurityScheme()
                        .name(bearerSchemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT"));
    }
}
