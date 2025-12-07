package com.suyos.user.exception.exceptions;

import org.springframework.http.HttpStatus;

import com.suyos.common.exception.ApiException;
import com.suyos.common.exception.ErrorCode;

public class UserNotFoundException extends ApiException {
    
    public UserNotFoundException(String detail) {
        super(
            "User not found with " + detail,
            HttpStatus.NOT_FOUND,
            "/errors/user-not-found",
            ErrorCode.USER_NOT_FOUND
        );
    }

}