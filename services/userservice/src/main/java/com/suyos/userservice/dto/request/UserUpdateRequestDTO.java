package com.suyos.userservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for user profile update.
 * 
 * <p>Contains the fields of the user profile a user can modify.</p>
 * 
 * @author Joel Salazar
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdateRequestDTO {
    
    /** First name */
    @NotBlank(message = "First name is required")
    @Pattern(
        regexp = "^[\\p{L} ]+$", 
        message = "First name must contain only letters and spaces"
    )
    private String firstName;
    
    /** Last name */
    @NotBlank(message = "Last name is required")
    @Pattern(
        regexp = "^[\\p{L} ]+$", 
        message = "Last name must contain only letters and spaces"
    )
    private String lastName;
    
    /** Phone number */
    @Pattern(
        regexp = "^$|^\\+?[0-9]{7,15}$",
        message = "Phone must be 7–15 digits, optional + for country code"
    )
    private String phone;
    
    /** Profile picture URL */
    private String profilePictureUrl;
    
    /** Preferred language locale */
    private String locale;
    
    /** Preferred timezone */
    private String timezone;
    
}