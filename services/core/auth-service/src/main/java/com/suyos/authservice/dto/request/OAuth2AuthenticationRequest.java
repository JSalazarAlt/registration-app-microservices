package com.suyos.authservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Data Transfer Object for OAuth2 authentication requests.
 * 
 * <p>Contains information from OAuth2 providers to authenticate an account
 * during login or account creation.</p>
 */
@Getter
@AllArgsConstructor
@Builder
public class OAuth2AuthenticationRequest {

    /** OAuth2 account email address */
    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private final String email;

    /** OAuth2 account name */
    @NotBlank(message = "Name is required")
    private final String name;

    /** OAuth2 provider */
    private final String provider;

    /** OAuth2 provider ID */
    @NotBlank(message = "Provider ID is required")
    private final String providerId;

    /** Client device name */
    @NotBlank(message = "Device name is required")
    private final String deviceName;
    
}