package com.suyos.authservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for email verification token requests.
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
public class RefreshTokenRequestDTO {

    /** Token value used for the request */
    @NotBlank(message = "Refresh token value is required")
    private String value;

}