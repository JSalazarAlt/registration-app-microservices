package com.suyos.authservice.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SessionTerminationReason {

    /** Session terminated by explicit logout from one single session */
    LOGOUT("Session terminated by explicit logout"),

    /** Session terminated by user */
    USER_TERMINATED("Session terminated by user"),

    /** Session terminated due to refresh token expiration */
    EXPIRED("Session terminated due to refresh token expiration"),

    /** Session terminated by server or security event */
    REVOKED("Session terminated by server or security event"),

    /** Session terminated after password change */
    PASSWORD_CHANGED("Session terminated after password change"),

    /** Session terminated due to account soft-deletion */
    ACCOUNT_SOFT_DELETED("Session terminated because account was soft-deleted"),

    /** Session terminated by admin */
    ADMIN_TERMINATED("Session terminated by administrator");

    /** Description of termination reason */
    private final String description;

}