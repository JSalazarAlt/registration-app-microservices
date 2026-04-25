package com.suyos.authservice.dto.response;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class AccountResponse {

    private final UUID id;

    private final String username;

    private final String email;

    private final Boolean emailVerified;
    
    private final Boolean mfaEnabled;

}