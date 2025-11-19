package com.suyos.authservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for forgot password requests.
 * 
 * <p>Contains the user's email address used to send a password reset link.</p>
 * 
 * @author Joel Salazar
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordForgotRequestDTO {

    /** Email address to send the password reset link */
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
    
}