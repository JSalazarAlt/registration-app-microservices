package com.suyos.auth.exception.exceptions;

import org.springframework.http.HttpStatus;

import com.suyos.common.exception.ApiException;
import com.suyos.common.exception.ErrorCode;

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