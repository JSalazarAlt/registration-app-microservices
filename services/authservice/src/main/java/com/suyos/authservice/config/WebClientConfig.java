package com.suyos.authservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configuration for WebClient beans used in interservice communication.
 *
 * <p>Configures WebClient instances for making HTTP requests to other
 * microservices in the system with appropriate base URLs and settings.</p>
 *
 * @author Joel Salazar
 */
@Configuration
public class WebClientConfig {

    /**
     * Creates WebClient bean for User Service communication.
     * 
     * @param builder WebClient builder
     * @return Configured WebClient for User Service
     */
    @Bean
    public WebClient userServiceWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl("http://localhost:8081")
                .build();
    }
    
}