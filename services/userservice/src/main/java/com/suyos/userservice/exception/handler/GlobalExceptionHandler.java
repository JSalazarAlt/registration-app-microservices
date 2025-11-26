package com.suyos.userservice.exception.handler;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import com.suyos.common.exception.ApiErrorResponse;
import com.suyos.common.exception.ApiException;
import com.suyos.common.exception.ErrorCode;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiErrorResponse> handleApiException(ApiException ex, WebRequest request) {
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

        return ResponseEntity.status(ex.getStatus()).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
            errors.put(error.getField(), error.getDefaultMessage())
        );

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

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleException(Exception ex, WebRequest request) {
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

        return ResponseEntity.internalServerError().body(response);
    }

    private String getTraceId() {
        String traceId = MDC.get("traceId");
        return (traceId != null && !traceId.isEmpty()) ? traceId : UUID.randomUUID().toString();
    }

}