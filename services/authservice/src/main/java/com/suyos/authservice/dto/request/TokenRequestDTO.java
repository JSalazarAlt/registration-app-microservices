package com.suyos.authservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for token-related requests.
 *
 * <p>Used for logout and token refresh operations that require
 * a refresh token for validation and processing.</p>
 *
 * @author Joel Salazar
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenRequestDTO {

    /** Token value used for the request */
    private String value;

}