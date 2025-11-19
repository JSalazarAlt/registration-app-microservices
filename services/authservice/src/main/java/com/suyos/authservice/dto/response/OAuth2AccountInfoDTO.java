package com.suyos.authservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Data Transfer Object for OAuth2 account information from Google.
 * 
 * <p>Contains standardized user details received from an OAuth2 provider
 * used for authentication and account linking.</p>
 * 
 * @author Joel Salazar
 */
@Getter
@AllArgsConstructor
@Builder
public class OAuth2AccountInfoDTO {

    /** Email address from OAuth2 provider */
    private String email;

    /** Flag indicating if email address has been verified by OAuth2 provider */
    @Builder.Default
    private Boolean emailVerified = true;

    /** OAuth2 provider name (e.g., Google, Facebook) */
    private String provider;

    /** Unique identifier from OAuth2 provider */
    private String providerId;

    /** Full name from OAuth2 provider */
    private String name;

    /** First name extracted from full name */
    private String firstName;

    /** Last name extracted from full name */
    private String lastName;

    /** Profile picture URL from OAuth2 provider */
    private String profilePictureUrl;
    
}