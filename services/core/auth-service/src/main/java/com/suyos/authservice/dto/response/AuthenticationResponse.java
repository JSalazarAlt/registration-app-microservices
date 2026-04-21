package com.suyos.authservice.dto.response;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Data Transfer Object for authentication responses.
 * 
 * <p>Contains the authenticated account identifier and access and refresh
 * tokens.</p>
 */
@Getter
@AllArgsConstructor
@Builder
public class AuthenticationResponse {

    /** Authenticated account ID */
    private final UUID accountId;

    /** JWT access token for API authentication */
    private final String accessToken;
    
    /** JWT token type */
    @Builder.Default
    private final String tokenType = "Bearer";
    
    /** JWT access token expiration time in seconds */
    private final Long accessTokenExpiresIn;

    /** Refresh token for renewing expired access tokens */
    private final String refreshToken;
    
}