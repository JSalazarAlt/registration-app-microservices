package com.suyos.authservice.exception.exceptions;

import org.springframework.http.HttpStatus;

import com.suyos.common.exception.ApiException;
import com.suyos.common.exception.ErrorCode;

/**
 * Exception thrown when attempting to access a disabled account.
 * 
 * <p>Indicates the account has been disabled by an administrator and
 * cannot be accessed until re-enabled.</p>
 */
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