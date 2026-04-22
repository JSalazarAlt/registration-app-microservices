package com.suyos.authservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Data Transfer Object for account authentication requests.
 * 
 * <p>Contains the account identifier and password to authenticate an account
 * and device name from which a user is logging in to create a session.</p>
 */
@Getter
@AllArgsConstructor
@Builder
public class AuthenticationRequest {

    /** Username or email */
    @NotBlank(message = "Username or email is required")
    private final String identifier;

    /** Password */
    @NotBlank(message = "Password is required")
    private final String password;

    /** Device name (e.g., iPhone 12, Dell XPS 13) */
    @NotBlank(message = "Device name is required")
    private final String deviceName;

}