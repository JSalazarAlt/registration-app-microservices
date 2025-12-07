package com.suyos.auth.exception.exceptions;

import org.springframework.http.HttpStatus;

import com.suyos.common.exception.ApiException;
import com.suyos.common.exception.ErrorCode;

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