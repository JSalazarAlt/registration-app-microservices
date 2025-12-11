package com.suyos.authservice.exception.exceptions;

import org.springframework.http.HttpStatus;

import com.suyos.common.exception.ApiException;
import com.suyos.common.exception.ErrorCode;

/**
 * Exception thrown when OAuth2 authentication fails.
 * 
 * <p>Indicates authentication with the specified OAuth2 provider
 * could not be completed successfully.</p>
 */
public class OAuth2AuthenticationFailerException extends ApiException {

    public OAuth2AuthenticationFailerException(String provider) {
        super(
            "OAuth2 authentication with " + provider + " failed",
            HttpStatus.UNAUTHORIZED,
            "/errors/oauth2-authentication-failed",
            ErrorCode.OAUTH2_AUTHENTICATION_FAILED
        );
    }
    
}