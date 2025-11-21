package com.suyos.common.event;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Event published when an account's email is updated.
 * 
 * @author Joel Salazar
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountEmailUpdatedEvent {
    
    /** Account ID */
    private UUID accountId;
    
    /** New email address */
    private String newEmail;
    
}
