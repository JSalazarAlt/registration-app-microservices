package com.suyos.authservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class RegistrationRequest {

    // ----------------------------------------------------------------
    // ACCOUNT CREDENTIALS
    // ----------------------------------------------------------------

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 20, message = "Username must be 3–20 characters")
    @Pattern(
        regexp = "^[a-zA-Z0-9]+$", 
        message = "Username must contain only alphanumeric characters"
    )
    private final String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private final String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#])[A-Za-z\\d@$!%*?&#]{8,}$",
        message = "Password must contain at least one uppercase letter, one lowercase letter, one number, and one special character"
    )
    private final String password;

    // ----------------------------------------------------------------
    // USER PROFILE
    // ----------------------------------------------------------------

    @NotBlank(message = "First name is required")
    @Pattern(
        regexp = "^[\\p{L} ]+$", 
        message = "First name must contain only letters and spaces"
    )
    private final String firstName;

    @NotBlank(message = "Last name is required")
    @Pattern(
        regexp = "^[\\p{L} ]+$", 
        message = "Last name must contain only letters and spaces"
    )
    private final String lastName;

    @Pattern(
        regexp = "^$|^\\+?[0-9]{7,15}$",
        message = "Phone must be 7–15 digits, optional + for country code"
    )
    private final String phoneNumber;
    
    private final String profilePictureUrl;
    
    private final String locale;
    
    private final String timezone;
    
}