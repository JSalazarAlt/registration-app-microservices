package com.suyos.authservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Data transfer object for email verification requests.
 *
 * <p>Contains the email verification token to verify an account email.</p>
 */
@Getter
@AllArgsConstructor
@Builder
public class EmailVerificationRequest {

    /** Unique value of email verification token  */
    @NotBlank(message = "Email verification token value is required")
    private final String value;
    
}