package com.suyos.authservice.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for user profile information.
 * 
 * <p>Contains public profile details used for display and client-facing API
 * responses without exposing sensitive data.</p>
 * 
 * @author Joel Salazar
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileDTO {

    /** User's unique identifier from User microservice */
    private UUID userId;

    /** Email address */
    private String email;

    /** Username */
    private String username;

    /** First name */
    private String firstName;

    /** Last name */
    private String lastName;

    /** Phone number */
    private String phone;

    /** Profile picture URL */
    private String profilePictureUrl;

    /** Preferred language locale */
    private String locale;

    /** Preferred timezone */
    private String timezone;

    /** Timestamp when terms of service were accepted */
    private LocalDateTime termsAcceptedAt;

    /** Timestamp when privacy policy was accepted */
    private LocalDateTime privacyPolicyAcceptedAt;

    /** Timestamp when user record was first created */
    private LocalDateTime createdAt;

    /** Timestamp when user record was last updated */
    private LocalDateTime updatedAt;

}