package com.suyos.common.exception;

import java.time.Instant;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiErrorResponse {
    
    // ----------------------------------------------------------------
    // STANDARD (RFC 7807)
    // ----------------------------------------------------------------

    /** RFC 7807 URI identifying the problem type (e.g., /docs/errors/not-found) */
    private String type;

    /** Short description of the error */
    private String title;

    /** HTTP status code (e.g., 400, 404) */
    private int status;

    /** Detailed error message */
    private String detail;

    /** Request path */
    private String path;
    
    // ----------------------------------------------------------------
    // APPLICATION-SPECIFIC
    // ----------------------------------------------------------------
    
    /** Custom fields for validation errors */
    private Map<String, String> validationErrors;

    /** Error name (e.g., ACCOUNT_NOT_FOUND)  */
    private ErrorCode code;
    
    /** Trace ID for debugging purposes */
    private String traceId;

    /** Timestamp when the error occurred */
    @Builder.Default
    private Instant timestamp = Instant.now();

}