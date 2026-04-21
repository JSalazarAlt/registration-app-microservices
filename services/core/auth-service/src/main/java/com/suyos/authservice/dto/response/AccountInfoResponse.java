package com.suyos.authservice.dto.response;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Data Transfer Object for basic account information.
 *
 * <p>Contains the identifier, username, email, and some flags.</p>
 */
@Getter
@AllArgsConstructor
@Builder
public class AccountInfoResponse {

    /** Unique identifier */
    private final UUID id;

    /** Username */
    private final String username;

    /** Email address */
    private final String email;

    /** Flag indicating if email address has been verified */
    private final Boolean emailVerified;

    /** Flag indicating if multi-factor authentication is enabled */
    private final Boolean mfaEnabled;

}