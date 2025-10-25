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

    /** Accounts's username for authentication */
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 20, message = "Username must be 3–20 characters")
    @Pattern(
        regexp = "^[a-zA-Z0-9]+$", 
        message = "Username must contain only alphanumeric characters"
    )
    private String username;

    /** Accounts's email address for authentication */
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    /** Accounts's password for authentication */
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;
    
}