package com.suyos.common.event;

import java.time.Instant;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Event published when an account's username is updated.
 * 
 * <p>Notifies the User microservice to synchronize username changes made in
 * the Auth microservice to maintain data consistency across services.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountUsernameUpdateEvent {

    // ----------------------------------------------------------------
    // EVENT METADATA
    // ----------------------------------------------------------------

    /** Unique identifier for the event */
    private String id;

    /** Timestamp when event ocurred */
    private Instant occurredAt;

    // ----------------------------------------------------------------
    // ACCOUNT INFORMATION
    // ----------------------------------------------------------------
    
    /** Unique identifier linking to account */
    private UUID accountId;
    
    /** Updated username */
    private String newUsername;
    
}