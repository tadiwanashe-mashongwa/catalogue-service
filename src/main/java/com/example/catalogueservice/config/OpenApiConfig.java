package com.example.catalogueservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI catalogueOpenApi() {
        return new OpenAPI().info(new Info()
                .title("SpareLink Catalogue Service API")
                .version("v1")
                .description("Catalogue management and spare-part lookup API."));
    }
}
