package com.suyos.authservice.exception.exceptions;

import org.springframework.http.HttpStatus;

import com.suyos.common.exception.ApiException;
import com.suyos.common.exception.ErrorCode;

public class InvalidRefreshTokenException extends ApiException {

    public InvalidRefreshTokenException() {
        super(
            "Invalid refresh token",
            HttpStatus.UNAUTHORIZED,
            "/errors/invalid-token",
            ErrorCode.INVALID_TOKEN
        );
    }
    
}
