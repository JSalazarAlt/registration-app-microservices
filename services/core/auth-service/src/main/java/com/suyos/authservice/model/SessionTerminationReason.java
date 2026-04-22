package com.suyos.authservice.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Enumeration of session termination reasons.
 * 
 * <p>Defines the reasons why a session might be terminated.</p>
 */
@Getter
@AllArgsConstructor
public enum SessionTerminationReason {

    LOGOUT("Session terminated by explicit logout"),

    USER_TERMINATED("Session terminated by user"),

    EXPIRED("Session terminated due to refresh token expiration"),

    REVOKED("Session terminated by server or security event"),

    PASSWORD_CHANGED("Session terminated after password change"),

    ACCOUNT_SOFT_DELETED("Session terminated because account was soft-deleted"),

    ADMIN_TERMINATED("Session terminated by administrator");

    private final String description;

}