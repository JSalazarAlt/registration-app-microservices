package com.suyos.auth.exception.exceptions;

import org.springframework.http.HttpStatus;

import com.suyos.common.exception.ApiException;
import com.suyos.common.exception.ErrorCode;

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