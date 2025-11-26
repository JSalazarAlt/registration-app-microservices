package com.suyos.authservice.exception.exceptions;

import org.springframework.http.HttpStatus;

import com.suyos.common.exception.ApiException;
import com.suyos.common.exception.ErrorCode;

public class AccountNotFoundException extends ApiException {
    
    public AccountNotFoundException(String id) {
        super(
            "Account with ID '" + id + "' not found",
            HttpStatus.NOT_FOUND,
            "/errors/account-not-found",
            ErrorCode.ACCOUNT_NOT_FOUND
        );
    }

}