package com.suyos.sessionservice.exception.exceptions;

import org.springframework.http.HttpStatus;

import com.suyos.common.exception.ApiException;
import com.suyos.common.exception.ErrorCode;

/**
 * Exception thrown when a session cannot be found.
 * 
 * <p>Indicates no session exists with the specified ID.</p>
 */
public class SessionNotFoundException extends ApiException {

    public SessionNotFoundException(String detail) {
        super(
            "Session not found with " + detail,
            HttpStatus.NOT_FOUND,
            "/docs/errors/session-not-found", 
            ErrorCode.ACCOUNT_DELETED
        );
    }
    
}