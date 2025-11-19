package com.suyos.authservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for refresh token requests.
 *
 * <p>Contains the refresh token value used to obtain a new access token or
 * invalidate an existing session.</p>
 * 
 * @author Joel Salazar
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshTokenRequestDTO {

    /** Refresh token value */
    @NotBlank(message = "Refresh token value is required")
    private String value;

}