package com.suyos.authservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Data Transfer Object for mobile authentication responses.
 * 
 * <p>Contains the access and refresh tokens used for session management,
 * and the linked account ID.</p>
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class MobileAuthenticationResponse extends BaseAuthenticationResponse {

    /** Refresh token for renewing expired access tokens */
    private String refreshToken;
    
}