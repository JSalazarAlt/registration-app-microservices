package com.suyos.authservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class EmailResendRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private final String email;
    
}