package com.suyos.common.event;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Event published when an account's username is updated.
 * 
 * @author Joel Salazar
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountUsernameUpdatedEvent {
    
    /** Account ID */
    private UUID accountId;
    
    /** New username */
    private String newUsername;
    
}
