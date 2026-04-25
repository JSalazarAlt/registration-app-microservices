package com.suyos.authservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class OAuth2AuthenticationRequest {

    /** Account email address from OAuth2 provider */
    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private final String email;

    /** Account full name from OAuth2 provider */
    @NotBlank(message = "Name is required")
    private final String name;

    private final String provider;

    @NotBlank(message = "OAuth2 provider ID is required")
    private final String providerId;

    /** Device name (e.g., iPhone 12, Dell XPS 13) */
    @NotBlank(message = "Device name is required")
    private final String deviceName;
    
}