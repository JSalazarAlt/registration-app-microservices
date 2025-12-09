package com.suyos.authservice.exception.exceptions;

import org.springframework.http.HttpStatus;

import com.suyos.common.exception.ApiException;
import com.suyos.common.exception.ErrorCode;

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