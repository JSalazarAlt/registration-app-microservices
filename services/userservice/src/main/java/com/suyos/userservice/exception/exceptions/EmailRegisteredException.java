package com.suyos.userservice.exception.exceptions;

import com.suyos.common.exception.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class EmailRegisteredException extends RuntimeException {
    
    /** */
    private final String message;
    
    /** */
    private final ErrorCode code = ErrorCode.EMAIL_ALREADY_REGISTERED;

}