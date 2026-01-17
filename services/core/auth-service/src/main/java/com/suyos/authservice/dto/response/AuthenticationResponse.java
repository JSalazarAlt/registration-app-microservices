package com.suyos.authservice.dto.response;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Data Transfer Object for authentication responses.
 * 
 * <p>Contains the access and refresh tokens used for session management,
 * and the linked account ID.</p>
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class AuthenticationResponse {

    /** Authenticated account's ID */
    private UUID accountId;

    /** JWT access token for API authentication */
    private String accessToken;
    
    /** JWT token type */
    @Builder.Default
    private String tokenType = "Bearer";
    
    /** JWT access token expiration time in seconds */
    private Long accessTokenExpiresIn;

    /** Refresh token for renewing expired access tokens */
    private String refreshToken;
    
}