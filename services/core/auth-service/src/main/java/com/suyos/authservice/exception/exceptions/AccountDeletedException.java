package com.suyos.authservice.exception.exceptions;

import org.springframework.http.HttpStatus;

import com.suyos.common.exception.ApiException;
import com.suyos.common.exception.ErrorCode;

/**
 * Exception thrown when attempting to access a deleted account.
 * 
 * <p>Indicates the account has been soft deleted and can be restored
 * by logging in again.</p>
 */
public class AccountDeletedException extends ApiException {

    public AccountDeletedException() {
        super(
            "Account has been deleted. Login to restore it",
            HttpStatus.FORBIDDEN,
            "/docs/errors/account-deleted", 
            ErrorCode.ACCOUNT_DELETED
        );
    }
    
}