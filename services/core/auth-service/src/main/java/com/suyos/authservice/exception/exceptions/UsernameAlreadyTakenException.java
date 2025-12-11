package com.suyos.authservice.exception.exceptions;

import org.springframework.http.HttpStatus;

import com.suyos.common.exception.ApiException;
import com.suyos.common.exception.ErrorCode;

/**
 * Exception thrown when attempting to register with an existing
 * username.
 * 
 * <p>Indicates the username is already associated with another
 * account and cannot be used for registration.</p>
 */
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