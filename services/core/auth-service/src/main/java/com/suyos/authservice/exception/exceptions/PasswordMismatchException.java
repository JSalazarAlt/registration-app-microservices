package com.suyos.authservice.exception.exceptions;

import org.springframework.http.HttpStatus;

import com.suyos.common.exception.ApiException;
import com.suyos.common.exception.ErrorCode;

/**
 * Exception thrown when password and confirmation do not match.
 * 
 * <p>Indicates the password and password confirmation fields contain
 * different values during registration or password change.</p>
 */
public class PasswordMismatchException extends ApiException {
    
    public PasswordMismatchException() {
        super(
            "Password and confirmation password do not match",
            HttpStatus.BAD_REQUEST,
            "/errors/password-mismatch",
            ErrorCode.PASSWORD_MISMATCH
        );
    }

}