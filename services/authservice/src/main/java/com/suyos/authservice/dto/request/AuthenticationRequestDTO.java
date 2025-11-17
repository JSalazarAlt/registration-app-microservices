package com.suyos.authservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for login authentication credentials.
 * 
 * <p>Captures and validates account credentials during the login process. 
 * Contains only essential fields required for authentication and session
 * establishment.</p>
 * 
 * @author Joel Salazar
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthenticationRequestDTO {

    /** Account's username or email */
    @NotBlank(message = "Username or email is required")
    private String identifier;

    /** Account's password */
    @NotBlank(message = "Password is required")
    private String password;

}