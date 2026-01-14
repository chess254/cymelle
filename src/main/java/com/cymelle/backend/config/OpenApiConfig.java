package com.cymelle.backend.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@SecurityScheme(
        name = "bearerAuth",
        description = "JWT Authentication using Bearer Token. Provide the token without the 'Bearer ' prefix.",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT"
)
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Cymelle API")
                        .version("1.0")
                        .description("Full Stack Developer Test API for Cymelle Technologies platform, managing products, users, orders, and rides.")
                        .contact(new Contact()
                                .name("Cymelle Support")
                                .url("https://github.com/chess254/cymelle")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .tags(List.of(
                        new Tag().name("Authentication").description("Endpoints for user registration and JWT token generation"),
                        new Tag().name("Products").description("Endpoints for catalog management. Viewing is public, modifications require ADMIN role."),
                        new Tag().name("Orders").description("Endpoints for placing and managing ecommerce orders"),
                        new Tag().name("Rides").description("Endpoints for the ride-hailing service")
                ));
    }
}
