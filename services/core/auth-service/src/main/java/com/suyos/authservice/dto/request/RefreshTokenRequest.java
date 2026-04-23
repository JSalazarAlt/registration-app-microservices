package com.suyos.authservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Data transfer object for refresh token requests.
 *
 * <p>Contains the refresh token to obtain a new access token or invalidate
 * an existing session during logout.</p>
 */
@Getter
@AllArgsConstructor
@Builder
public class RefreshTokenRequest {

    /** Unique value of refresh token */
    @NotBlank(message = "Refresh token value is required")
    private final String value;

}