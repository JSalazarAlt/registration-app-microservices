package com.suyos.authservice.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Enumeration of account roles.
 * 
 * <p>Defines the roles that can be assigned to accounts.</p>
 */
@Getter
@AllArgsConstructor
public enum AccountRole {

    USER("Standard user with basic application access."),

    ADMIN("Administrator with elevated privileges."),

    SUPER_ADMIN("Super administrator with full access."),
    
    SYSTEM("System account with no restrictions.");

    private final String description;

}