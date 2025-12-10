package com.suyos.authservice.exception.exceptions;

import org.springframework.http.HttpStatus;

import com.suyos.common.exception.ApiException;
import com.suyos.common.exception.ErrorCode;

/**
 * Exception thrown when attempting to access a locked account.
 * 
 * <p>Indicates the account is temporarily locked due to multiple
 * failed login attempts and will be unlocked after a specified
 * duration.</p>
 */
public class AccountLockedException extends ApiException {
    
    public AccountLockedException(String unlockTime) {
        super(
            "Account is locked due to multiple failed login attempts. Try again after " + unlockTime + " minutes",
            HttpStatus.LOCKED,
            "/errors/account-locked",
            ErrorCode.ACCOUNT_LOCKED
        );
    }

}