package com.suyos.auth.dto.response;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Data Transfer Object for authentication responses.
 * 
 * <p>Contains the access and refresh tokens used for session management,
 * and the linked account ID.</p>
 */
@Getter
@AllArgsConstructor
@Builder
public class AuthenticationResponseDTO {
    
    /** Authenticated account's ID */
    private UUID accountId;

    /** Refresh token for renewing expired access tokens */
    private String refreshToken;

    /** JWT access token for API authentication */
    private String accessToken;
    
    /** JWT token type */
    @Builder.Default
    private String tokenType = "Bearer";
    
    /** JWT access token expiration time in seconds */
    private Long accessTokenExpiresIn;

}