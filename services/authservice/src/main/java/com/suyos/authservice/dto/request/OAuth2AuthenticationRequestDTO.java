package com.suyos.authservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for OAuth2 authentication requests.
 * 
 * <p>Captures user information from OAuth2 providers (Google,
 * Facebook, etc.) during authentication. Used for both account
 * creation and login flows.</p>
 * 
 * @author Joel Salazar
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OAuth2AuthenticationRequestDTO {

    /** OAuth2 account's email address */
    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    /** OAuth2 account's name */
    @NotBlank(message = "Name is required")
    private String name;

    /** OAuth2 provider */
    private String provider;

    /** OAuth2 provider ID */
    @NotBlank(message = "Provider ID is required")
    private String providerId;
    
}