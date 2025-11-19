package com.suyos.authservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.Builder;

/**
 * Configuration for WebClient beans used in interservice communication.
 *
 * <p>Configures WebClient instances for making HTTP requests to other
 * microservices in the system with appropriate URLs and settings.</p>
 *
 * @author Joel Salazar
 */
@Configuration
public class WebClientConfig {

    /**
     * Creates WebClient bean for User microservice communication.
     * 
     * @param builder WebClient builder
     * @return Configured WebClient for User Service
     */
    @Bean
    public WebClient sessionMicroserviceWebClient(Builder builder) {
        return builder
                .baseUrl("http://localhost:8081")
                .build();
    }
    
    /**
     * Creates WebClient bean for User microservice communication.
     * 
     * @param builder WebClient builder
     * @return Configured WebClient for User Service
     */
    @Bean
    public WebClient userMicroserviceWebClient(Builder builder) {
        return builder
                .baseUrl("http://localhost:8081")
                .build();
    }

    /**
     * Creates WebClient bean for Email microservice communication.
     * 
     * @param builder WebClient builder
     * @return Configured WebClient for User Service
     */
    @Bean
    public WebClient emailMicroserviceWebClient(Builder builder) {
        return builder
                .baseUrl("http://localhost:8083")
                .build();
    }
    
}