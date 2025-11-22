package com.suyos.authservice.exception.exceptions;

import com.suyos.common.exception.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class AccountLockedException extends RuntimeException {
    
    /** */
    private final String message;
    
    /** */
    private final ErrorCode code = ErrorCode.ACCOUNT_LOCKED;

}