package com.suyos.auth.dto.response;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Data Transfer Object for basic account information.
 *
 * <p>Contains basic account's information.</p>
 */
@Getter
@AllArgsConstructor
@Builder
public class AccountInfoDTO {

    /** Account's ID */
    private UUID id;

    /** Username */
    private String username;

    /** Email address */
    private String email;

    /** Flag indicating if email address has been verified */
    private Boolean emailVerified;

    /** Flag indicating if multi-factor authentication is enabled */
    private Boolean mfaEnabled;

}