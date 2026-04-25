package com.suyos.authservice.dto.response;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class AuthenticationResponse {

    private final UUID accountId;

    private final String accessToken;
    
    /** JWT access token type */
    @Builder.Default
    private final String tokenType = "Bearer";
    
    /** JWT access token expiration time in seconds */
    private final Long accessTokenExpiresIn;

    private final String refreshToken;
    
}