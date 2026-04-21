package com.suyos.authservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Data Transfer Object for password reset requests.
 *
 * <p>Contains the password reset token and a new password to complete the
 * password reset process.</p>
 */
@Getter
@AllArgsConstructor
@Builder
public class PasswordResetRequest {

    /** Password reset token value */
    @NotBlank(message = "Password reset token value is required")
    private final String value;

    /** New password */
    @NotBlank(message = "New password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
        message = "Password must contain at least one uppercase letter, one lowercase letter, one number, and one special character"
    )
    private final String newPassword;
    
}