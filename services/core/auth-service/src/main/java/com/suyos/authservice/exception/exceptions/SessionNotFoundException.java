package com.suyos.authservice.exception.exceptions;

import org.springframework.http.HttpStatus;

import com.suyos.common.exception.ApiException;
import com.suyos.common.exception.ErrorCode;

public class SessionNotFoundException extends ApiException {

    public SessionNotFoundException(String detail) {
        super(
            "Session not found with " + detail,
            HttpStatus.NOT_FOUND,
            "/docs/errors/session-not-found", 
            ErrorCode.SESSION_NOT_FOUND
        );
    }
    
}