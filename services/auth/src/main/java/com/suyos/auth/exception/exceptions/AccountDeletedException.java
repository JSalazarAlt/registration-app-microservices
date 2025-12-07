package com.suyos.auth.exception.exceptions;

import org.springframework.http.HttpStatus;

import com.suyos.common.exception.ApiException;
import com.suyos.common.exception.ErrorCode;

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