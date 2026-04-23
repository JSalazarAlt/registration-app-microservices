package com.suyos.authservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Data transfer object for resend email verification requests.
 * 
 * <p>Contains the account's email address to send a new email verification
 * link.</p>
 */
@Getter
@AllArgsConstructor
@Builder
public class EmailResendRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private final String email;
    
}