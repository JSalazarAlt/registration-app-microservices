package com.suyos.userservice.dto.response;

import java.time.Instant;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for user profile.
 * 
 * <p>Contains user's profile.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileDTO {

    /** User's unique identifier */
    private UUID id;

    /** User's email address */
    private String email;

    /** User's chosen username for display purposes */
    private String username;

    /** User's first name for personalization */
    private String firstName;

    /** User's last name for identification */
    private String lastName;

    /** User's phone number for contact purposes */
    private String phone;

    /** URL to the user's profile picture */
    private String profilePictureUrl;

    /** User's preferred language locale */
    private String locale;

    /** User's timezone preference */
    private String timezone;

    /** Timestamp when the user accepted the terms of service */
    private Instant termsAcceptedAt;

    /** Timestamp when the user accepted the privacy policy */
    private Instant privacyPolicyAcceptedAt;

    /** Timestamp when the user record was first created in the system */
    private Instant createdAt;

    /** Timestamp when the user record was first updated in the system */
    private Instant updatedAt;

}