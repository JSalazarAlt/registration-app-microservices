package com.suyos.authservice.exception.exceptions;

import org.springframework.http.HttpStatus;

import com.suyos.common.exception.ApiException;
import com.suyos.common.exception.ErrorCode;

/**
 * Exception thrown when a refresh token is invalid or expired.
 * 
 * <p>Indicates the refresh token cannot be used to obtain new access
 * tokens and the user must re-authenticate.</p>
 */
public class InvalidRefreshTokenException extends ApiException {

    public InvalidRefreshTokenException() {
        super(
            "Invalid refresh token",
            HttpStatus.UNAUTHORIZED,
            "/errors/invalid-token",
            ErrorCode.INVALID_TOKEN
        );
    }
    
}
