package com.suyos.authservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class EmailVerificationRequest {

    /** Unique value of email verification token  */
    @NotBlank(message = "Email verification token value is required")
    private final String value;
    
}