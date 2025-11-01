package com.suyos.authservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for user registration and update information.
 * 
 * <p>This DTO is used to capture and validate user credentials during registration 
 * process. It contains only the essential fields required for user authentication 
 * and session establishment.</p>
 * 
 * @author Joel Salazar
 */
@Data

@NoArgsConstructor
@AllArgsConstructor
public class AccountUpsertDTO {

    /** Account's username */
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 20, message = "Username must be 3–20 characters")
    @Pattern(
        regexp = "^[a-zA-Z0-9]+$", 
        message = "Username must contain only alphanumeric characters"
    )
    private String username;

    /** Account's email address */
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    /** Account's password */
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    /** User's first name */
    @NotBlank(message = "First name is required")
    @Pattern(
        regexp = "^[\\p{L} ]+$", 
        message = "First name must contain only letters and spaces"
    )
    private String firstName;

    /** User's last name */
    @NotBlank(message = "Last name is required")
    @Pattern(
        regexp = "^[\\p{L} ]+$", 
        message = "Last name must contain only letters and spaces"
    )
    private String lastName;

    /** User's phone number for contact purposes */
    @Pattern(
        regexp = "^$|^\\+?[0-9]{7,15}$",
        message = "Phone must be 7–15 digits, optional + for country code"
    )
    private String phone;
    
    /** URL to the user's profile picture */
    private String profilePictureUrl;
    
    /** User's preferred language locale */
    private String locale;
    
    /** User's timezone preference */
    private String timezone;
    
}