package com.suyos.userservice.dto.response;

import java.time.Instant;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class UserResponse {

    // ----------------------------------------------------------------
    // IDENTITY
    // ----------------------------------------------------------------

    private final UUID id;

    // ----------------------------------------------------------------
    // ACCOUNT'S CREDENTIALS
    // ----------------------------------------------------------------

    private final String email;

    private final String username;

    // ----------------------------------------------------------------
    // PROFILE
    // ----------------------------------------------------------------

    private final String firstName;

    private final String lastName;

    private final String phoneNumber;

    private final String profilePictureUrl;

    // ----------------------------------------------------------------
    // PREFERENCES
    // ----------------------------------------------------------------

    private final String locale;

    private final String timezone;

    // ----------------------------------------------------------------
    // LEGAL TERMS
    // ----------------------------------------------------------------

    private final Instant termsAcceptedAt;

    private final Instant privacyPolicyAcceptedAt;

}