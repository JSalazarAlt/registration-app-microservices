package com.suyos.authservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Data transfer object for OAuth2 account information.
 * 
 * <p>Contains basic account's information from an OAuth2 provider.</p>
 */
@Getter
@AllArgsConstructor
@Builder
public class OAuth2AccountResponse {

    private final String email;

    private final Boolean emailVerified;

    /** OAuth2 provider name (e.g., Google, Facebook) */
    private final String provider;

    /** Unique identifier from OAuth2 provider */
    private final String providerId;

    /** Full name from OAuth2 provider */
    private final String providerName;

    /** First name extracted from full name */
    private final String firstName;

    /** Last name extracted from full name */
    private final String lastName;

    /** Profile picture URL from OAuth2 provider */
    private final String profilePictureUrl;
    
}