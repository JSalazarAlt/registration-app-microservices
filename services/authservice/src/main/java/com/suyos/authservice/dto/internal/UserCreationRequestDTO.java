package com.suyos.authservice.dto.internal;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for user creation requests to User microservice.
 *
 * <p>Contains account and user information needed to create a user profile
 * in the User microservice during account registration.</p>
 *
 * @author Joel Salazar
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCreationRequestDTO {

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