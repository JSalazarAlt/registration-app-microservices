package com.suyos.auth.exception.exceptions;

import org.springframework.http.HttpStatus;

import com.suyos.common.exception.ApiException;
import com.suyos.common.exception.ErrorCode;

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