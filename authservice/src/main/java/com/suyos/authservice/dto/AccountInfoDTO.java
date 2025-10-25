package com.suyos.authservice.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class AccountInfoDTO {

    /** Accounts's ID */
    private UUID id;

    /** Accounts's username */
    private String username;

    /** Accounts's email address */
    private String email;

}