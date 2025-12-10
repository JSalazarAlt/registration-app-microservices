package com.suyos.authservice.exception.exceptions;

import org.springframework.http.HttpStatus;

import com.suyos.common.exception.ApiException;
import com.suyos.common.exception.ErrorCode;

/**
 * Exception thrown when attempting to access account with unverified
 * email.
 * 
 * <p>Indicates the email address must be verified before the account
 * can be accessed.</p>
 */
public class EmailNotVerifiedException extends ApiException {
    
    public EmailNotVerifiedException(String email) {
        super(
            "Email '" + email + "' has not been verified. Please check your inbox",
            HttpStatus.FORBIDDEN,
            "/errors/email-not-verified",
            ErrorCode.EMAIL_NOT_VERIFIED
        );
    }

}