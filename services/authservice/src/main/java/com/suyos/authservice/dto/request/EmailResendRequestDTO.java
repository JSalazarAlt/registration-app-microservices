package com.suyos.authservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for resend email verification requests.
 * 
 * <p>Contains the account's email address used to send a new email
 * verification link.</p>
 * 
 * @author Joel Salazar
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailResendRequestDTO {

    /** Email address to send the email verification link */
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
    
}