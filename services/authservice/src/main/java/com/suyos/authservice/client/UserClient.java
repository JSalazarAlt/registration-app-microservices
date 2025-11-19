package com.suyos.authservice.client;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.suyos.authservice.dto.internal.UserCreationRequestDTO;
import com.suyos.authservice.dto.response.UserProfileDTO;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * Web client for interservice communication with User Service.
 *
 * <p>Handles HTTP requests to User microservice endpoints for user-related
 * operations during authentication flows.</p>
 *
 * @author Joel Salazar
 */
@Component
@RequiredArgsConstructor
public class UserClient {

    /** WebClient configured for User microservice communication */
    private final WebClient userServiceWebClient;

    /**
     * Creates a new user in the User microservice.
     * 
     * @param request Account and user data
     * @return Mono containing created user's profile
     */
    public Mono<UserProfileDTO> createUser(UserCreationRequestDTO request) {
        return userServiceWebClient
                .post()
                .uri(uriBuilder -> uriBuilder
                        .path("/internal/users")
                        .queryParam("accountId", request.getAccountId())
                        .queryParam("username", request.getUsername())
                        .queryParam("email", request.getEmail())
                        .build())
                .bodyValue(request)
                .retrieve()
                .bodyToMono(UserProfileDTO.class);
    }
    
}