package com.suyos.authservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for authentication requests.
 * 
 * <p>Contains the account identifier and password used to authenticate the
 * user and establish a session.</p>
 * 
 * @author Joel Salazar
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthenticationRequestDTO {

    /** Username or email */
    @NotBlank(message = "Username or email is required")
    private String identifier;

    /** Password */
    @NotBlank(message = "Password is required")
    private String password;

}