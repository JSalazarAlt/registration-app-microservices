package com.suyos.authservice.exception.exceptions;

import org.springframework.http.HttpStatus;

import com.suyos.common.exception.ApiException;
import com.suyos.common.exception.ErrorCode;

/**
 * Exception thrown when an account cannot be found.
 * 
 * <p>Indicates no account exists with the specified identifier such
 * as username, email, or account ID.</p>
 */
public class AccountNotFoundException extends ApiException {
    
    public AccountNotFoundException(String detail) {
        super(
            "Account not found with " + detail,
            HttpStatus.NOT_FOUND,
            "/errors/account-not-found",
            ErrorCode.ACCOUNT_NOT_FOUND
        );
    }

}