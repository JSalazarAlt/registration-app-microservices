package com.suyos.userservice.exception.exceptions;

import org.springframework.http.HttpStatus;

import com.suyos.common.exception.ApiException;
import com.suyos.common.exception.ErrorCode;

/**
 * Exception thrown when a request is duplicated.
 * 
 * <p>Indicates the request is duplicated and must not be processed. Used
 * for idempotency purposes.</p>
 */
public class DuplicateEventException extends ApiException {
    
    public DuplicateEventException(String detail) {
        super(
            "Duplicate event detected with " + detail,
            HttpStatus.CONFLICT,
            "/docs/errors/duplicate-event",
            ErrorCode.DUPLICATE_EVENT
        );
    }

}