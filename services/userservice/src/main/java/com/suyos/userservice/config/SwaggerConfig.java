package com.suyos.userservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

/**
 * Swagger/OpenAPI configuration for API documentation.
 * 
 * <p>Configures OpenAPI 3.0 documentation with JWT authentication support. 
 * Provides comprehensive API documentation accessible via Swagger UI.</p>
 * 
 * @author Joel Salazar
 */
@Configuration
public class SwaggerConfig {

    /**
     * Configures OpenAPI documentation with user microservice information.
     * 
     * @return OpenAPI configuration object
     */
    @Bean
    public OpenAPI expenseTrackerOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Registration API")
                .description("REST API for the User microservice of the registration application")
                .version("1.0.0")
                .contact(new Contact()
                    .name("Joel Salazar")
                    .email("ed.joel.salazar@gmail.com"))
                .license(new License()
                    .name("MIT License")
                    .url("https://opensource.org/licenses/MIT")))
            .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
            .components(new Components()
                .addSecuritySchemes("Bearer Authentication", 
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("Enter JWT token obtained from login endpoint")));
    }
    
}