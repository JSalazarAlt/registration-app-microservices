package com.suyos.authservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for email verification token requests.
 *
 * <p>Used for email verication operations that require an email
 * for processing.</p>
 *
 * @author Joel Salazar
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailVerificationRequestDTO {

    /** Token value used for the request */
    private String value;
    
}
