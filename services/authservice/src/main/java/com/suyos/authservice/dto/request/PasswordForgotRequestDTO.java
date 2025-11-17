package com.suyos.authservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordForgotRequestDTO {

    /** Email address to which send the password reset link */
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
    
}