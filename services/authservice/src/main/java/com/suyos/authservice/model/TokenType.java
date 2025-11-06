package com.suyos.authservice.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Enumeration of token types within the authentication system.
 * 
 * <p>This enum defines the various types of tokens used for authentication,
 * each with specific purposes and usage scenarios.</p>
 * 
 * @author Joel Salazar
 */
@Getter
@AllArgsConstructor
public enum TokenType {

    /** Access Token */
    ACCESS("Token used for authenticating API requests."),

    /** Refresh Token */
    REFRESH("Token used for obtaining new access tokens."),

    /** Email Verification Token */
    EMAIL_VERIFICATION("Token used for verifying user email addresses."),

    /** Password Reset Token */
    PASSWORD_RESET("Token used for resetting user passwords."),

    /** Multi-Factor Authentication Token */
    MFA_VERIFICATION("Token used for verifying user identity.");

    /** Description of the token type */
    private String description;

}