package com.suyos.authservice.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Enumeration of token types within the authentication system.
 * 
 * <p>Defines the types of tokens used for authentication.</p>
 */
@Getter
@AllArgsConstructor
public enum TokenType {

    ACCESS("Token used for authenticating API requests."),

    REFRESH("Token used for obtaining new access tokens."),

    EMAIL_VERIFICATION("Token used for verifying user email addresses."),

    PASSWORD_RESET("Token used for resetting user passwords."),

    MFA_VERIFICATION("Token used for verifying user identity.");

    private final String description;

}