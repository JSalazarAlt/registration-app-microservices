package com.suyos.authservice.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Data Transfer Object for account information.
 *
 * <p>Contains basic account details for public display and API responses.
 * Used when returning account information without sensitive data.</p>
 *
 * @author Joel Salazar
 */
@Getter
@AllArgsConstructor
@Builder
public class AccountInfoDTO {

    /** Account's ID */
    private UUID id;

    /** Account's username */
    private String username;

    /** Account's email address */
    private String email;

}