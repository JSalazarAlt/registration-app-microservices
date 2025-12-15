package com.suyos.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Enumeration of standardized error codes.
 * 
 * <p>Defines error codes used across microservices for consistent error
 * handling and client-side error processing.</p>
 * 
 * @author Joel Salazar
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {

    // ----------------------------------------------------------------
    //  ACCOUNT / USER STATE ERRORS
    // ----------------------------------------------------------------

    /** Account resource does not exist */
    ACCOUNT_NOT_FOUND("Account not found"),

    /** Account is temporarily locked */
    ACCOUNT_LOCKED("Account is temporarily locked"),

    /** Account has been disabled by administrator */
    ACCOUNT_DISABLED("Account has been disabled"),

    /** Account has been deleted */
    ACCOUNT_DELETED("Account has been deleted"),

    /** User resource does not exist */
    USER_NOT_FOUND("User not found"),

    /** Account email has not been verified yet */
    EMAIL_NOT_VERIFIED("Email address has not been verified"),

    /** Email already verified */
    EMAIL_ALREADY_VERIFIED("Email is already verified"),

    // ----------------------------------------------------------------
    //  UNIQUE CONSTRAINT ERRORS
    // ----------------------------------------------------------------

    /** Provided email is already registered in the system */
    EMAIL_ALREADY_REGISTERED("Email is already registered"),

    /** Username is already taken by another account */
    USERNAME_ALREADY_TAKEN("Username is already taken"),

    // ----------------------------------------------------------------
    //  AUTHENTICATION ERRORS
    // ----------------------------------------------------------------

    /** Authentication failed due to invalid credentials (email/username/password) */
    INVALID_CREDENTIALS("Invalid email or password"),

    /** Password is incorrect */
    INVALID_PASSWORD("Invalid password"),

    /** Password does not meet minimum security requirements */
    WEAK_PASSWORD("Password does not meet security requirements"),

    /** Password and confirmation password do not match */
    PASSWORD_MISMATCH("Passwords do not match"),

    // ----------------------------------------------------------------
    //  TOKEN AND SECURITY ERRORS
    // ----------------------------------------------------------------

    /** Token resource does not exist */
    TOKEN_NOT_FOUND("Token not found"),

    /** Provided token is invalid (malformed or signature mismatch) */
    INVALID_TOKEN("Invalid authentication token"),

    /** Provided refresh token is invalid */
    INVALID_REFRESH_TOKEN("Invalid refresh token"),

    /** Invalid email verification token */
    INVALID_EMAIL_VERIFICATION_TOKEN("Invalid email verification token"),

    /** Invalid password token */
    INVALID_PASSWORD_TOKEN("Invalid password token"),

    /** Provided token has expired */
    TOKEN_EXPIRED("Authentication token has expired"),

    /** Provided token has been revoked and is no longer valid */
    TOKEN_REVOKED("Token has been revoked"),

    /** Refresh token has expired */
    REFRESH_TOKEN_EXPIRED("Refresh token has expired"),

    /** Caller is not authorized to access the requested resource */
    ACCESS_DENIED("Access denied"),

    // ----------------------------------------------------------------
    //  OAUTH2 ERRORS
    // ----------------------------------------------------------------

    /** OAuth2 authentication process failed */
    OAUTH2_AUTHENTICATION_FAILED("OAuth2 authentication failed"),

    /** OAuth2 provider returned an error */
    OAUTH2_PROVIDER_ERROR("OAuth2 provider error"),

    // ----------------------------------------------------------------
    //  OPERATIONAL / BUSINESS LOGIC ERRORS
    // ----------------------------------------------------------------

    /** Requested operation is not allowed for this resource */
    OPERATION_NOT_ALLOWED("Operation not allowed"),

    /** Resource state conflicts with the requested operation */
    RESOURCE_CONFLICT("Resource conflict"),

    // ----------------------------------------------------------------
    //  VALIDATION ERRORS
    // ----------------------------------------------------------------

    /** Request contains invalid or malformed input data */
    INVALID_INPUT("Invalid input data"),

    /** Required field is missing from the request */
    MISSING_REQUIRED_FIELD("Required field is missing"),

    /** Request payload or parameters failed validation checks */
    VALIDATION_ERROR("Validation error"),


    SESSION_NOT_FOUND("Session not found"),

    // ----------------------------------------------------------------
    //  INTERNAL SERVER ERRORS
    // ----------------------------------------------------------------

    /** Unexpected internal error occurred while processing request */
    INTERNAL_ERROR("Internal server error");

    /** Short description of the error code */
    private final String description;

}