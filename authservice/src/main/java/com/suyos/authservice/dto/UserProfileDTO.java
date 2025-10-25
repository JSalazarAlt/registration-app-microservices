package com.suyos.authservice.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for user profile information.
 * 
 * <p>This DTO is used to transfer user profile data from the API to clients for 
 * display purposes. It contains public user information that can be safely 
 * exposed in API responses without sensitive security data.</p>
 * 
 * @author Joel Salazar
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
    private LocalDateTime termsAcceptedAt;

    /** Timestamp when the user accepted the privacy policy */
    private LocalDateTime privacyPolicyAcceptedAt;

    /** Timestamp when the user record was first created in the system */
    private LocalDateTime createdAt;

    /** Timestamp when the user record was first updated in the system */
    private LocalDateTime updatedAt;

}