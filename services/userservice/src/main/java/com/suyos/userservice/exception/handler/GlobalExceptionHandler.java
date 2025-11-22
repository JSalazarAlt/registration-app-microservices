package com.suyos.userservice.exception.handler;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import com.suyos.common.exception.ApiErrorResponse;
import com.suyos.common.exception.ErrorCode;
import com.suyos.userservice.exception.exceptions.EmailRegisteredException;
import com.suyos.userservice.exception.exceptions.UserNotFoundException;
import com.suyos.userservice.exception.exceptions.UsernameTakenException;

/**
 * Global exception handler for User Service.
 * 
 * <p>Handles all exceptions and returns standardized error responses.</p>
 * 
 * @author Joel Salazar
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ApiErrorResponse handleAccountNotFound(UserNotFoundException ex, WebRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), ex.getCode(), request);
    }

    @ExceptionHandler(EmailRegisteredException.class)
    public ApiErrorResponse handleEmailAlreadyUsed(EmailRegisteredException ex, WebRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), ex.getCode(), request);
    }

    @ExceptionHandler(UsernameTakenException.class)
    public ApiErrorResponse handleUsernameAlreadyUsed(UsernameTakenException ex, WebRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), ex.getCode(), request);
    }

    @ExceptionHandler(Exception.class)
    public ApiErrorResponse handleOther(Exception ex, WebRequest request) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), ErrorCode.INTERNAL_ERROR, request);
    }

    private ApiErrorResponse build(HttpStatus status, String message, ErrorCode code, WebRequest request) {
        return ApiErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .code(code)
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
    }

}