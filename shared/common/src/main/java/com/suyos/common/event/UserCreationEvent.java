package com.suyos.common.event;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Event published when an account's email is updated.
 * 
 * @author Joel Salazar
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCreationEvent {

    // ----------------------------------------------------------------
    // ACCOUNT INFORMATION
    // ----------------------------------------------------------------

    /** Account ID from Auth microservice */
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