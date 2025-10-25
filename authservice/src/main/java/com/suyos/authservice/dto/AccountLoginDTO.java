package com.suyos.authservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for user login authentication information.
 * 
 * <p>This DTO is used to capture and validate user credentials during the login 
 * process. It contains only the essential fields required for user authentication 
 * and session establishment.</p>
 * 
 * @author Joel Salazar
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountLoginDTO {

    /** Accounts's username for authentication */
    private String username;

    /** Accounts's email address for authentication */
    @Email(message = "Invalid email format")
    private String email;

    /** Accounts's password for authentication */
    @NotBlank(message = "Password is required")
    private String password;

}