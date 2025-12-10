package com.suyos.authservice.exception.exceptions;

import org.springframework.http.HttpStatus;

import com.suyos.common.exception.ApiException;
import com.suyos.common.exception.ErrorCode;

/**
 * Exception thrown when an OAuth2 provider returns an error.
 * 
 * <p>Indicates the OAuth2 provider encountered an issue and could
 * not complete the authentication request.</p>
 */
public class OAuth2ProviderError extends ApiException {

    public OAuth2ProviderError(String provider) {
        super(
            "OAuth2 provider " + provider + " returned an error",
            HttpStatus.BAD_GATEWAY,
            "/errors/oauth2-provider-error",
            ErrorCode.OAUTH2_PROVIDER_ERROR
        );
    }
    
}