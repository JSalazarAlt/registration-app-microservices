package com.suyos.common.event;

import java.time.Instant;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Event published when a new user account is created.
 * 
 * <p>Contains all necessary information to create a corresponding user
 * profile in the User microservice when an account is registered in the
 * Auth microservice.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCreationEvent {

    // ----------------------------------------------------------------
    // EVENT METADATA
    // ----------------------------------------------------------------

    /** Unique identifier for the event */
    private String id;

    /** Timestamp when event ocurred */
    private Instant occurredAt;

    // ----------------------------------------------------------------
    // ACCOUNT INFORMATION
    // ----------------------------------------------------------------

    /** Unique identifier linking to account */
    private UUID accountId;

    /** Username */
    private String username;

    /** Email address */
    private String email;

    // ----------------------------------------------------------------
    // USER PROFILE
    // ----------------------------------------------------------------

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
    
}