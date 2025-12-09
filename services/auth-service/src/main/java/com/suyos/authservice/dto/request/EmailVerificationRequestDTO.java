package com.suyos.authservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for email verification requests.
 *
 * <p>Contains the email verification token to verify an account's email.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailVerificationRequestDTO {

    /** Email verification token value */
    private String value;
    
}