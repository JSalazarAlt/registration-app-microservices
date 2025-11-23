package com.suyos.common.event;

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
 * 
 * @author Joel Salazar
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountUsernameUpdateEvent {
    
    /** Account ID from Auth microservice */
    private UUID accountId;
    
    /** Updated username */
    private String newUsername;
    
}