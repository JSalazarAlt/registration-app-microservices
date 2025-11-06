package com.suyos.authservice.dto.internal;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for user creation requests to User Service.
 *
 * <p>Contains account and profile information needed to create a user
 * profile in the User Service during account registration.</p>
 *
 * @author Joel Salazar
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCreationRequestDTO {

    /** Account ID from Auth Service */
    private UUID accountId;

    /** Account's username */
    private String username;

    /** Account's email address */
    private String email;

    /** User's first name */
    private String firstName;
    
    /** User's last name */
    private String lastName;

    /** User's phone number */
    private String phone;
    
    /** URL to the user's profile picture */
    private String profilePictureUrl;
    
    /** User's preferred language locale */
    private String locale;
    
    /** User's timezone preference */
    private String timezone;

}