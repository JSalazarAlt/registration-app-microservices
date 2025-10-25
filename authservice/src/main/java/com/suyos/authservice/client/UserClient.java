package com.suyos.authservice.client;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.suyos.authservice.dto.UserCreationRequestDTO;
import com.suyos.authservice.dto.UserProfileDTO;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class UserClient {

    private final WebClient userServiceWebClient;

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