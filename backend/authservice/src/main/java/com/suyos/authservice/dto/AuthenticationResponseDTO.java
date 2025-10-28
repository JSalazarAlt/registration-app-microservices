package com.suyos.authservice.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Data Transfer Object for authentication response containing JWT token and user 
 * information.
 * 
 * <p>This DTO is returned after successful authentication, providing the client
 * with a JWT access token for subsequent API requests and basic user profile 
 * information.</p>
 * 
 * @author Joel Salazar
 */
@Getter
@AllArgsConstructor
@Builder
public class AuthenticationResponseDTO {
    
    /** Authenticated account's ID (for cross-service reference) */
    private UUID accountId;
    
    /** JWT access token for API authentication */
    private String accessToken;

    /** Refresh token for renewing expired access tokens */
    private String refreshToken;
    
    /** Token type identifier */
    @Builder.Default
    private String tokenType = "Bearer";
    
    /** Token expiration time in seconds */
    private Long expiresIn;

}