package com.suyos.authservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for account authentication requests.
 * 
 * <p>Contains an account's identifier and password to authenticate the account
 * and the device name from which a user is logging in to create a session.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthenticationRequest {

    /** Username or email */
    @NotBlank(message = "Username or email is required")
    private String identifier;

    /** Password */
    @NotBlank(message = "Password is required")
    private String password;

    /** Device name from which a user is logging in */
    @NotBlank(message = "Device name is required")
    private String deviceName;

}