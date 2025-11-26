package com.suyos.authservice.exception.exceptions;

import org.springframework.http.HttpStatus;

import com.suyos.common.exception.ApiException;
import com.suyos.common.exception.ErrorCode;

public class AccountLockedException extends ApiException {
    
    public AccountLockedException(String unlockTime) {
        super(
            "Account is locked due to multiple failed login attempts. Try again after " + unlockTime,
            HttpStatus.FORBIDDEN,
            "/errors/account-locked",
            ErrorCode.ACCOUNT_LOCKED
        );
    }

}