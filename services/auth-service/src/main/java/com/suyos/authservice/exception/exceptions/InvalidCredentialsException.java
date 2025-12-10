package com.suyos.authservice.exception.exceptions;

import org.springframework.http.HttpStatus;

import com.suyos.common.exception.ApiException;
import com.suyos.common.exception.ErrorCode;

/**
 * Exception thrown when authentication fails due to invalid credentials.
 * 
 * <p>Indicates the provided username/email or password does not match
 * any existing account.</p>
 */
public class InvalidCredentialsException extends ApiException {
    
    public InvalidCredentialsException() {
        super(
            "Invalid email/username or password",
            HttpStatus.UNAUTHORIZED,
            "/errors/invalid-credentials",
            ErrorCode.INVALID_CREDENTIALS
        );
    }

}