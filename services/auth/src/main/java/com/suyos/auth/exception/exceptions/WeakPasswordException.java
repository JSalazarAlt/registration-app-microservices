package com.suyos.auth.exception.exceptions;

import org.springframework.http.HttpStatus;

import com.suyos.common.exception.ApiException;
import com.suyos.common.exception.ErrorCode;

public class WeakPasswordException extends ApiException {
    
    public WeakPasswordException() {
        super(
            "Password does not meet security requirements. Must be at least 8 characters with uppercase, lowercase, and numbers",
            HttpStatus.BAD_REQUEST,
            "/errors/weak-password",
            ErrorCode.WEAK_PASSWORD
        );
    }

}