package com.suyos.authservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Data transfer object for authentication requests.
 * 
 * <p>Contains the account's username or email, password, and device name to
 * authenticate an account.</p>
 */
@Getter
@AllArgsConstructor
@Builder
public class AuthenticationRequest {

    @NotBlank(message = "Username or email is required")
    private final String identifier;

    @NotBlank(message = "Password is required")
    private final String password;

    /** Device name (e.g., iPhone 12, Dell XPS 13) */
    @NotBlank(message = "Device name is required")
    private final String deviceName;

}