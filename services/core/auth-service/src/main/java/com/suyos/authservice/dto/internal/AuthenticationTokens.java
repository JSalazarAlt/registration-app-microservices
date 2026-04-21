package com.suyos.authservice.dto.internal;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object for authentication responses.
 * 
 * <p>Contains the authenticated account identifier and access and refresh
 * tokens.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthenticationTokens {
    
    /** Authenticated account ID */
    private UUID accountId;

    /** JWT access token for API authentication */
    private String accessToken;
    
    /** JWT token type */
    @Builder.Default
    private String tokenType = "Bearer";
    
    /** JWT access token expiration time in seconds */
    private Long accessTokenExpiresIn;

    /** Refresh token value for renewing expired access tokens */
    private String refreshToken;
    
}