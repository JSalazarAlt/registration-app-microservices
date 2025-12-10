package com.suyos.authservice.exception.exceptions;

import org.springframework.http.HttpStatus;

import com.suyos.authservice.model.TokenType;
import com.suyos.common.exception.ApiException;
import com.suyos.common.exception.ErrorCode;

/**
 * Exception thrown when a token is invalid, expired, or revoked.
 * 
 * <p>Indicates the token of the specified type cannot be used and a
 * new token must be obtained.</p>
 */
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