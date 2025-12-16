package com.suyos.common.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SessionTerminationReason {

    /** Session ended by explicit logout */
    SINGLE_LOGOUT("Session ended by explicit logout"),

    GLOBAL_LOGOUT("Session ended by explicit global logout"),

    /** Session expired due to refresh token expiration */
    EXPIRED("Session expired due to refresh token expiration"),

    /** Session revoked by server or security event */
    REVOKED("Session revoked by server or security event"),

    /** Session invalidated after password change */
    PASSWORD_CHANGED("Session invalidated after password change"),

    /** Session removed because account was deleted */
    ACCOUNT_DELETED("Session removed because account was deleted");

    /** Description of termination reason */
    private final String description;

}