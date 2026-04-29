package com.suyos.userservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class UserUpdateRequest {

    // ----------------------------------------------------------------
    // PROFILE
    // ----------------------------------------------------------------
    
    @NotBlank(message = "First name is required")
    @Pattern(
        regexp = "^[\\p{L} ]+$", 
        message = "First name must contain only letters and spaces"
    )
    private final String firstName;
    
    @NotBlank(message = "Last name is required")
    @Pattern(
        regexp = "^[\\p{L} ]+$", 
        message = "Last name must contain only letters and spaces"
    )
    private final String lastName;
    
    @Pattern(
        regexp = "^$|^\\+?[0-9]{7,15}$",
        message = "Phone must be 7–15 digits, optional + for country code"
    )
    private final String phoneNumber;
    
    private final String profilePictureUrl;

    // ----------------------------------------------------------------
    // PREFERENCES
    // ----------------------------------------------------------------
    
    private final String locale;
    
    private final String timezone;
    
}