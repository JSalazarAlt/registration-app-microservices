package com.suyos.authservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Data Transfer Object for forgot password requests.
 * 
 * <p>Contains the email address to send a password reset link.</p>
 */
@Getter
@AllArgsConstructor
@Builder
public class PasswordForgotRequest {

    /** Email address to send the password reset link */
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private final String email;
    
}