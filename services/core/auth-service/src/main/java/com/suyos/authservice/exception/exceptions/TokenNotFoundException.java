package com.suyos.authservice.exception.exceptions;

import org.springframework.http.HttpStatus;

import com.suyos.common.exception.ApiException;
import com.suyos.common.exception.ErrorCode;

/**
 * Exception thrown when a token cannot be found.
 * 
 * <p>Indicates no token exists with the specified value in the
 * database.</p>
 */
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