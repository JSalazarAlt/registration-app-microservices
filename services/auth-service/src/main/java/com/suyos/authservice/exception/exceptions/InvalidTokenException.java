package com.suyos.authservice.exception.exceptions;

import org.springframework.http.HttpStatus;

import com.suyos.authservice.model.TokenType;
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