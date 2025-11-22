package com.suyos.userservice.exception.exceptions;

import com.suyos.common.exception.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UserNotFoundException extends RuntimeException {
    
    /** */
    private final String message;
    
    /** */
    private final ErrorCode code = ErrorCode.USER_NOT_FOUND;

}