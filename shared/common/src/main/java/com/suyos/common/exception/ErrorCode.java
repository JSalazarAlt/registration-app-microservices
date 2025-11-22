package com.suyos.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Enumeration of account roles.
 * 
 * <p>Defines the various codes that are intended to be used when throwing
 * or mapping exceptions to API error responses.</p>
 * 
 * @author Joel Salazar
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {

    /** Account resource does not exist */
    ACCOUNT_NOT_FOUND("Account not found"),

    /** User resource does not exist */
    USER_NOT_FOUND("User not found"),

    /** Token resource does not exist */
    TOKEN_NOT_FOUND("Token not found"),

    /** Provided email is already registered in the system */
    EMAIL_ALREADY_REGISTERED("Email is already registered"),

    /** Username is already taken by another account */
    USERNAME_ALREADY_TAKEN("Username is already taken"),

    /** Authentication failed due to invalid credentials (email/username/password) */
    INVALID_CREDENTIALS("Invalid email or password"),

    /** Provided token is invalid (malformed or signature mismatch) */
    INVALID_TOKEN("Invalid authentication token"),

    /** Provided token has expired */
    TOKEN_EXPIRED("Authentication token has expired"),

    /** Caller is not authorized to access the requested resource */
    ACCESS_DENIED("Access denied"),
    
    /**  */
    EMAIL_NOT_VERIFIED(""),

    /**  */
    ACCOUNT_LOCKED(""),

    /**  */
    ACCOUNT_DISABLED(""),

    /** Request payload or parameters failed validation checks */
    VALIDATION_ERROR("Validation error"),

    /** Unexpected internal error occurred while processing the request */
    INTERNAL_ERROR("Internal server error");

    /** Short description of the error code */
    private final String description;

}