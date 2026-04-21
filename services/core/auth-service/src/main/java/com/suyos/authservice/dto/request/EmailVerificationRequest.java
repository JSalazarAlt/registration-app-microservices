package com.suyos.authservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Data Transfer Object for email verification requests.
 *
 * <p>Contains the email verification token to verify an account email.</p>
 */
@Getter
@AllArgsConstructor
@Builder
public class EmailVerificationRequest {

    /** Email verification token value */
    private final String value;
    
}