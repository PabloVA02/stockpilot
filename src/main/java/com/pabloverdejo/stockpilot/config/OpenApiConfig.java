package com.pabloverdejo.stockpilot.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI stockPilotOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("StockPilot API")
                        .version("1.0.0")
                        .description("REST API for products, stock movements and inventory indicators.")
                        .contact(new Contact().name("Pablo Verdejo Alonso")))
                .components(new Components().addSecuritySchemes("bearerAuth",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
