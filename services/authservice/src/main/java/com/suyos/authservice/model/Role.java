package com.suyos.authservice.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Enumeration of account roles within the authentication system.
 * 
 * <p>This enum defines the various roles that can be assigned to user accounts,
 * each with specific permissions and access levels.</p>
 * 
 * @author Joel Salazar
 */
@Getter
@AllArgsConstructor
public enum Role {

    /** Standard user role */
    USER("Standard user with basic application access."),

    /** Administrator role */
    ADMIN("Administrator with elevated privileges."),

    /** Super Administrator role */
    SUPER_ADMIN("Super administrator with full access."),
    
    /** System role */
    SYSTEM("System account with no restrictions.");

    /** Description of the role */
    private final String description;

}
