package com.suyos.authservice.exception.exceptions;

import org.springframework.http.HttpStatus;

import com.suyos.common.exception.ApiException;
import com.suyos.common.exception.ErrorCode;

/**
 * Exception thrown when attempting to verify an already verified email.
 * 
 * <p>Indicates the email address has already been verified and does
 * not require further verification.</p>
 */
public class EmailAlreadyVerifiedException extends ApiException {
    
    public EmailAlreadyVerifiedException(String email) {
        super(
            "Email '" + email + "' is already verified",
            HttpStatus.BAD_REQUEST,
            "/errors/email-already-verified",
            ErrorCode.EMAIL_ALREADY_VERIFIED
        );
    }

}