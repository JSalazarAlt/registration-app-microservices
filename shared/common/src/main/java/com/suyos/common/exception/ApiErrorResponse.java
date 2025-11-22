package com.suyos.common.exception;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Standardized error response for API exceptions.
 * 
 * @author Joel Salazar
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiErrorResponse {
    
    /** Timestamp when error occurred */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
    
    /** HTTP status code */
    private int status;
    
    /** Error type */
    private String error;
    
    /** Human-readable error message */
    private String message;
    
    /** API path where error occurred */
    private String path;
    
    /** Custom error code */
    private ErrorCode code;
    
}