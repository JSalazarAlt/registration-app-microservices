package com.suyos.common.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

/**
 * Base class for all custom, expected business logic errors.
 * 
 * <p>Enforces the inclusion of HTTP Status, RFC 7807 'type' URI, and the
 * ErrorCode.</p>
 * 
 * @author Joel Salazar
 */
@Getter 
public class ApiException extends RuntimeException {
    
    /** HTTP status code to return (e.g., 404, 409) */
    private final HttpStatus status;

    /** RFC 7807 URI identifying the problem type (e.g., /docs/errors/not-found) */
    private final String type;

    /** Error code value */
    private final ErrorCode errorCode;

    /**
     * Constructs a new ApiException using standardized error.
     *
     * @param status HTTP status code
     * @param type RFC 7807 URI
     * @param code Error code
     * @param detail Detailed error message
     */
    public ApiException(String detail, HttpStatus status, String type, ErrorCode errorCode) {
        super(detail); 
        this.status = status;
        this.type = type;
        this.errorCode = errorCode;
    }

}