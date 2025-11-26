package com.suyos.authservice.exception.exceptions;

import org.springframework.http.HttpStatus;

import com.suyos.common.exception.ApiException;
import com.suyos.common.exception.ErrorCode;

public class UsernameAlreadyTakenException extends ApiException {
    
    public UsernameAlreadyTakenException(String username) {
        super(
            "Username '" + username + "' is already taken",
            HttpStatus.CONFLICT,
            "/errors/username-already-taken",
            ErrorCode.USERNAME_ALREADY_TAKEN
        );
    }

}