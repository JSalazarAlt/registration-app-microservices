package com.suyos.authservice.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCreationRequestDTO {

    private UUID accountId;

    private String username;

    private String email;

    private String firstName;
    
    private String lastName;

    private String phone;
    
    /** URL to the user's profile picture */
    private String profilePictureUrl;
    
    /** User's preferred language locale */
    private String locale;
    
    /** User's timezone preference */
    private String timezone;

}