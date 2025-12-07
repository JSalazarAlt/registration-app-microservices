package com.suyos.auth.exception.exceptions;

import org.springframework.http.HttpStatus;

import com.suyos.common.exception.ApiException;
import com.suyos.common.exception.ErrorCode;

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