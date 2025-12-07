package com.suyos.auth.exception.exceptions;

import org.springframework.http.HttpStatus;

import com.suyos.auth.model.TokenType;
import com.suyos.common.exception.ApiException;
import com.suyos.common.exception.ErrorCode;

public class InvalidTokenException extends ApiException {
    
    public InvalidTokenException(TokenType tokenType) {
        super(
            "Invalid " + tokenType.name().toLowerCase().replace("_", " ") + " token",
            HttpStatus.GONE,
            "/errors/invalid-token",
            ErrorCode.INVALID_TOKEN
        );
    }

}