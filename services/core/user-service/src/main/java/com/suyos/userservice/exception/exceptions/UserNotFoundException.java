package com.suyos.userservice.exception.exceptions;

import org.springframework.http.HttpStatus;

import com.suyos.common.exception.ApiException;
import com.suyos.common.exception.ErrorCode;

/**
 * Exception thrown when a user cannot be found.
 * 
 * <p>Indicates no user exists with the specified identifier such as
 * user ID or account ID.</p>
 */
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