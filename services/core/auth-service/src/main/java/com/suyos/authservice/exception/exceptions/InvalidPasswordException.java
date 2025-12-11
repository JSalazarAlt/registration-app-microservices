package com.suyos.authservice.exception.exceptions;

import org.springframework.http.HttpStatus;

import com.suyos.common.exception.ApiException;
import com.suyos.common.exception.ErrorCode;

/**
 * Exception thrown when a password does not meet requirements.
 * 
 * <p>Indicates the provided password is invalid or does not match
 * the expected format.</p>
 */
public class InvalidPasswordException extends ApiException {
    
    public InvalidPasswordException() {
        super(
            "Invalid password",
            HttpStatus.BAD_REQUEST,
            "/errors/invalid-password",
            ErrorCode.INVALID_PASSWORD
        );
    }

}