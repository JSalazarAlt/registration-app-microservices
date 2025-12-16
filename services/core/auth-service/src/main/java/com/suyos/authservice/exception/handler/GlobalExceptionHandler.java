package com.suyos.authservice.exception.handler;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import com.suyos.common.exception.ApiErrorResponse;
import com.suyos.common.exception.ApiException;
import com.suyos.common.exception.ErrorCode;

/**
 * Global exception handler for REST controllers.
 *
 * <p>Centralized exception handling. Catches application-specific exceptions,
 * validation errors, and generic exceptions, to transform them into consistent
 * API error responses with trace IDs for debugging and monitoring.</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles custom API exceptions.
     *
     * <p>Converts {@link ApiException} into a standardized API error response
     * with appropriate HTTP status, error code, and trace ID for tracking.</p>
     *
     * @param ex API exception thrown
     * @param request Current web request
     * @return Error response with HTTP status
     */
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiErrorResponse> handleApiException(ApiException ex, WebRequest request) {
        // Build error response
        ApiErrorResponse response = ApiErrorResponse.builder()
                .type(ex.getType())
                .title(ex.getStatus().getReasonPhrase())
                .status(ex.getStatus().value())
                .detail(ex.getMessage())
                .path(request.getDescription(false).replace("uri=", ""))
                .code(ex.getErrorCode())
                .traceId(getTraceId())
                .timestamp(Instant.now())
                .build();

        // Return error response with HTTP status
        return ResponseEntity.status(ex.getStatus()).body(response);
    }

    /**
     * Handles Spring validation errors.
     *
     * <p>Converts {@link MethodArgumentNotValidException} (Bean Validation
     * failures) into structured error response containing validation errors.
     * Used when request body or parameters fail validation checks.</p>
     *
     * @param ex Validation exception thrown by Spring
     * @param request Current web request
     * @return Error response with "400 Bad Request" status
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex, WebRequest request) {
        // Extract field-level validation errors from binding result
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
            errors.put(error.getField(), error.getDefaultMessage())
        );

        // Build error response with validation errors
        ApiErrorResponse response = ApiErrorResponse.builder()
                .type("/errors/validation-error")
                .title("Bad Request")
                .status(400)
                .detail("Validation failed")
                .path(request.getDescription(false).replace("uri=", ""))
                .validationErrors(errors)
                .code(ErrorCode.VALIDATION_ERROR)
                .traceId(getTraceId())
                .timestamp(Instant.now())
                .build();

        // Return error response with "400 Bad Request" status
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Handles authentication failures thrown by Spring Security.
     *
     * <p>Converts {@link AuthenticationException} into a standardized
     * API error response indicating the caller is unauthenticated.
     * This occurs when credentials are missing, invalid, or expired.</p>
     *
     * @param ex Authentication exception thrown by Spring Security
     * @param request Current web request
     * @return Error response with "401 Unauthorized" status
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthException(AuthenticationException ex, WebRequest req) {
        ApiErrorResponse r = ApiErrorResponse.builder()
            .type("/errors/unauthorized")
            .title("Unauthorized")
            .status(401)
            .detail("Authentication failed")
            .path(req.getDescription(false).replace("uri=", ""))
            .code(ErrorCode.INVALID_TOKEN)
            .traceId(getTraceId())
            .timestamp(Instant.now())
            .build();
        return ResponseEntity.status(401).body(r);
    }

    /**
     * Handles access denied errors thrown by Spring Security.
     *
     * <p>Converts {@link AccessDeniedException} into a standardized API
     * error response indicating the authenticated principal does not have
     * permission to access the requested resource.</p>
     *
     * @param ex Access denied exception thrown by Spring Security
     * @param request Current web request
     * @return Error response with "403 Forbidden" status
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(AccessDeniedException ex, WebRequest req) {
        ApiErrorResponse r = ApiErrorResponse.builder()
            .type("/errors/forbidden")
            .title("Forbidden")
            .status(403)
            .detail("Access denied")
            .path(req.getDescription(false).replace("uri=", ""))
            .code(ErrorCode.ACCESS_DENIED)
            .traceId(getTraceId())
            .timestamp(Instant.now())
            .build();
        return ResponseEntity.status(403).body(r);
    }

    /**
     * Handles all uncaught exceptions.
     *
     * <p>Catch-all handler for unexpected exceptions not covered by specific
     * handlers. Converts any exception into generic 500 Internal Server Error
     * response.</p>
     *
     * @param ex Uncaught exception
     * @param request Current web request
     * @return Error response with "500 Internal Server Error" status
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleException(Exception ex, WebRequest request) {
        // Build generic internal error response
        ApiErrorResponse response = ApiErrorResponse.builder()
                .type("/errors/internal-error")
                .title("Internal Server Error")
                .status(500)
                .detail(ex.getMessage())
                .path(request.getDescription(false).replace("uri=", ""))
                .code(ErrorCode.INTERNAL_ERROR)
                .traceId(getTraceId())
                .timestamp(Instant.now())
                .build();

        // Return 500 Internal Server Error response
        return ResponseEntity.internalServerError().body(response);
    }

    /**
     * Retrieves or generates a trace ID for request tracking.
     *
     * <p>Attempts to retrieve an existing trace ID from SLF4J MDC (Mapped
     * Diagnostic Context). If not available or empty, generates a new UUID
     * for tracking this error across logs and distributed systems.</p>
     *
     * @return The trace ID from MDC or a newly generated UUID
     */
    private String getTraceId() {
        // Get trace ID from MDC if available
        String traceId = MDC.get("traceId");
        
        // Return existing trace ID or generate new UUID for tracking
        return (traceId != null && !traceId.isEmpty()) ? traceId : UUID.randomUUID().toString();
    }
    
}