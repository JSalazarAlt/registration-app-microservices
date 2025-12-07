package com.suyos.auth.exception.exceptions;

import org.springframework.http.HttpStatus;

import com.suyos.common.exception.ApiException;
import com.suyos.common.exception.ErrorCode;

public class AccountDisabledException extends ApiException {
    
    public AccountDisabledException() {
        super(
            "Account has been disabled by administrator",
            HttpStatus.FORBIDDEN,
            "/errors/account-disabled",
            ErrorCode.ACCOUNT_DISABLED
        );
    }
    
}