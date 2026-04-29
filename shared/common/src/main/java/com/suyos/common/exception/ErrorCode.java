package com.suyos.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // ----------------------------------------------------------------
    //  ACCOUNT MICROSERVICE ERRORS
    // ----------------------------------------------------------------

    // ACCOUNT STATUS ERRORS

    ACCOUNT_DELETED,

    ACCOUNT_DISABLED,

    ACCOUNT_LOCKED,

    ACCOUNT_NOT_FOUND,

    // EMAIL AND USERNAME ERRORS

    EMAIL_ALREADY_REGISTERED,

    EMAIL_ALREADY_VERIFIED,

    EMAIL_NOT_VERIFIED,

    USERNAME_ALREADY_TAKEN,

    // TOKEN AND SECURITY ERRORS

    INVALID_CREDENTIALS,

    INVALID_PASSWORD,

    INVALID_REFRESH_TOKEN,

    INVALID_TOKEN,

    TOKEN_NOT_FOUND,

    // OAUTH2 ERRORS

    OAUTH2_AUTHENTICATION_FAILED,

    OAUTH2_PROVIDER_ERROR,

    // PASSWORD ERRORS

    PASSWORD_MISMATCH,

    WEAK_PASSWORD,

    DUPLICATE_REQUEST,

    // ----------------------------------------------------------------
    //  USER MICROSERVICE ERRORS
    // ----------------------------------------------------------------

    USER_NOT_FOUND,

    // ----------------------------------------------------------------
    //  SESSION MICROSERVICE ERRORS
    // ----------------------------------------------------------------

    SESSION_NOT_FOUND,

    // ----------------------------------------------------------------
    //  OPERATIONAL / BUSINESS LOGIC ERRORS
    // ----------------------------------------------------------------

    ACCESS_DENIED,

    OPERATION_NOT_ALLOWED,

    RESOURCE_CONFLICT,

    DUPLICATE_EVENT,

    // ----------------------------------------------------------------
    //  VALIDATION ERRORS
    // ----------------------------------------------------------------

    INVALID_INPUT,

    MISSING_REQUIRED_FIELD,

    VALIDATION_ERROR,

    // ----------------------------------------------------------------
    //  INTERNAL SERVER ERRORS
    // ----------------------------------------------------------------

    INTERNAL_ERROR;

}