package com.suyos.authservice.exception.exceptions;

import org.springframework.http.HttpStatus;

import com.suyos.common.exception.ApiException;
import com.suyos.common.exception.ErrorCode;

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