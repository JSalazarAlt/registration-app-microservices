package com.suyos.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for account update requests.
 * 
 * <p>Contains the fields a user can modify to update their account.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountUpdateRequestDTO {

    /** New username */
    @Size(min = 3, max = 20, message = "Username must be 3–20 characters")
    @Pattern(
        regexp = "^[a-zA-Z0-9]+$", 
        message = "Username must contain only alphanumeric characters"
    )
    private String username;

    /** New email address */
    @Email(message = "Invalid email format")
    private String email;
    
}