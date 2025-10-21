package com.suyos.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for user profile update information.
 * 
 * <p>This DTO is used to capture and validate user input when updating profile 
 * information. It contains only the fields that users are allowed to modify.</p>
 * 
 * @author Joel Salazar
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdateDTO {
    
    /** User's first name for personalization */
    @NotBlank(message = "First name is required")
    @Pattern(
        regexp = "^[\\p{L} ]+$", 
        message = "First name must contain only letters and spaces"
    )
    private String firstName;
    
    /** User's last name for identification */
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