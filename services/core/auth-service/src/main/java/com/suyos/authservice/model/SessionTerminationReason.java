package com.suyos.authservice.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SessionTerminationReason {

    /** Session ended by explicit logout from one single session */
    SINGLE_LOGOUT("Session ended by explicit logout"),

    /** Session ended by explicit global logout from all sessions */
    GLOBAL_LOGOUT("Session ended by explicit global logout"),

    /** Session expired due to refresh token expiration */
    EXPIRED("Session expired due to refresh token expiration"),

    /** Session revoked by server or security event */
    REVOKED("Session revoked by server or security event"),

    /** Session invalidated after password change */
    PASSWORD_CHANGED("Session invalidated after password change"),

    /** Session removed due to account soft-deletion */
    ACCOUNT_SOFT_DELETED("Session removed because account was soft-deleted"),

    /** Session terminated by admin */
    ADMIN_TERMINATED("Session terminated by administrator");

    /** Description of termination reason */
    private final String description;

}