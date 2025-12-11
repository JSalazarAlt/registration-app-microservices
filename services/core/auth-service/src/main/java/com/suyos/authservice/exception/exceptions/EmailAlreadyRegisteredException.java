package com.suyos.authservice.exception.exceptions;

import org.springframework.http.HttpStatus;

import com.suyos.common.exception.ApiException;
import com.suyos.common.exception.ErrorCode;

/**
 * Exception thrown when attempting to register with an existing email.
 * 
 * <p>Indicates the email address is already associated with another
 * account and cannot be used for registration.</p>
 */
public class EmailAlreadyRegisteredException extends ApiException {
    
    public EmailAlreadyRegisteredException(String email) {
        super(
            "Email '" + email + "' is already registered",
            HttpStatus.CONFLICT,
            "/errors/email-already-registered",
            ErrorCode.EMAIL_ALREADY_REGISTERED
        );
    }

}