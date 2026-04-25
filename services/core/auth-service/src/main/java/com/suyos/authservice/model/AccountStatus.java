package com.suyos.authservice.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AccountStatus {

    ACTIVE("Account is fully operational with active login privileges."),

    DISABLED("Account is restricted from access, typically due to administrative action or security policy."),
    
    SOFT_DELETED("Account is logically removed from the system but retained in the database for auditing and recovery purposes.");

    private final String description;

}