package com.suyos.authservice.dto.response;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Data transfer object for authentication responses.
 * 
 * <p>Contains the authenticated account identifier and access and refresh
 * tokens.</p>
 */
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