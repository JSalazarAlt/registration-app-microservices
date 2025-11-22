package com.suyos.authservice.exception.handler;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import com.suyos.authservice.exception.exceptions.AccountDisabledException;
import com.suyos.authservice.exception.exceptions.AccountLockedException;
import com.suyos.authservice.exception.exceptions.AccountNotFoundException;
import com.suyos.authservice.exception.exceptions.EmailNotVerifiedException;
import com.suyos.authservice.exception.exceptions.EmailRegisteredException;
import com.suyos.authservice.exception.exceptions.InvalidCredentialsException;
import com.suyos.authservice.exception.exceptions.TokenNotFoundException;
import com.suyos.authservice.exception.exceptions.UsernameTakenException;
import com.suyos.common.exception.ApiErrorResponse;
import com.suyos.common.exception.ErrorCode;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidCredentialsException.class)
    public ApiErrorResponse handleInvalidCredentials(InvalidCredentialsException ex, WebRequest request) {
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage(), ex.getCode(), request);
    }

    @ExceptionHandler(AccountLockedException.class)
    public ApiErrorResponse handleAccountLocked(AccountLockedException ex, WebRequest request) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage(), ex.getCode(), request);
    }

    @ExceptionHandler(EmailRegisteredException.class)
    public ApiErrorResponse handleEmailAlreadyUsed(EmailRegisteredException ex, WebRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), ex.getCode(), request);
    }

    @ExceptionHandler(UsernameTakenException.class)
    public ApiErrorResponse handleUsernameAlreadyUsed(UsernameTakenException ex, WebRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), ex.getCode(), request);
    }

    @ExceptionHandler(AccountNotFoundException.class)
    public ApiErrorResponse handleAccountNotFound(AccountNotFoundException ex, WebRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), ex.getCode(), request);
    }

    @ExceptionHandler(TokenNotFoundException.class)
    public ApiErrorResponse handleTokenNotFound(TokenNotFoundException ex, WebRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), ex.getCode(), request);
    }

    @ExceptionHandler(EmailNotVerifiedException.class)
    public ApiErrorResponse handleEmailNotVerified(EmailNotVerifiedException ex, WebRequest request) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage(), ex.getCode(), request);
    }

    @ExceptionHandler(AccountDisabledException.class)
    public ApiErrorResponse handleAccountDisabled(AccountDisabledException ex, WebRequest request) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage(), ex.getCode(), request);
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