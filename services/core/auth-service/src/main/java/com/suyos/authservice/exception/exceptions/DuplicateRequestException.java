package com.suyos.authservice.exception.exceptions;

import org.springframework.http.HttpStatus;

import com.suyos.common.exception.ApiException;
import com.suyos.common.exception.ErrorCode;

/**
 * Exception thrown when a request is duplicated.
 * 
 * <p>Indicates the request is duplicated.</p>
 */
public class DuplicateRequestException extends ApiException {
    
    public DuplicateRequestException() {
        super(
            "Duplicate request detected",
            HttpStatus.CONFLICT,
            "/errors/duplicate-request",
            ErrorCode.DUPLICATE_REQUEST
        );
    }

}