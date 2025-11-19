package com.suyos.authservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for email verification requests.
 *
 * <p>Contains the verification token used to confirm a user's email during
 * the registration process.</p>
 * 
 * @author Joel Salazar
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailVerificationRequestDTO {

    /** Email verification token value */
    private String value;
    
}