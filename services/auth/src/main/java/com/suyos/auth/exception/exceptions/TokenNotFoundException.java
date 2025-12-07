package com.suyos.auth.exception.exceptions;

import org.springframework.http.HttpStatus;

import com.suyos.common.exception.ApiException;
import com.suyos.common.exception.ErrorCode;

public class TokenNotFoundException extends ApiException {
    
    public TokenNotFoundException(String value) {
        super(
            "Token not found with value=" + value,
            HttpStatus.NOT_FOUND,
            "/errors/token-not-found",
            ErrorCode.TOKEN_NOT_FOUND
        );
    }

}