package com.suyos.authservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Data Transfer Object for account update requests.
 * 
 * <p>Contains the account new username or email.</p>
 */
@Getter
@AllArgsConstructor
@Builder
public class AccountUpdateRequest {

    /** New username */
    @Size(min = 3, max = 20, message = "Username must be 3–20 characters")
    @Pattern(
        regexp = "^[a-zA-Z0-9]+$", 
        message = "Username must contain only alphanumeric characters"
    )
    private final String username;

    /** New email address */
    @Email(message = "Invalid email format")
    private final String email;
    
}