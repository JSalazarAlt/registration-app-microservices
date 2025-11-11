package com.suyos.authservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Data Transfer Object for OAuth2 account information from Google.
 * 
 * Standardizes user information received from Google OAuth2 provider
 * into a common format for processing.
 * 
 * @author Joel Salazar
 */
@Getter
@AllArgsConstructor
@Builder
public class OAuthAccountInfoDTO {

    /** User's email address from OAuth2 provider */
    private String email;

    /** User's full name from OAuth2 provider */
    private String name;

    /** User's first name extracted from full name */
    private String firstName;

    /** User's last name extracted from full name */
    private String lastName;

    /** URL to user's profile picture from OAuth2 provider */
    private String profilePictureUrl;

    /** OAuth2 provider name (e.g., Google) */
    private String provider;

    /** Unique identifier from OAuth2 provider */
    private String providerId;

    /** Whether email is verified by OAuth2 provider */
    @Builder.Default
    private Boolean emailVerified = true;
    
}