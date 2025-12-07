package com.suyos.auth.exception.exceptions;

import org.springframework.http.HttpStatus;

import com.suyos.common.exception.ApiException;
import com.suyos.common.exception.ErrorCode;

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