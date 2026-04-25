package com.suyos.authservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class RefreshTokenRequest {

    /** Unique value of refresh token */
    @NotBlank(message = "Refresh token value is required")
    private final String value;

}