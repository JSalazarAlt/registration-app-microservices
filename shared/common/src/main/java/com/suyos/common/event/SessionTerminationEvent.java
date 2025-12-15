package com.suyos.common.event;

import java.time.Instant;
import java.util.UUID;

import com.suyos.common.model.SessionTerminationReason;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionTerminationEvent {

    // ----------------------------------------------------------------
    // EVENT METADATA
    // ----------------------------------------------------------------

    /** Unique identifier for the event */
    private String id;

    /** Timestamp when event ocurred */
    private Instant occurredAt;

    // ----------------------------------------------------------------
    // SESSION'S INFORMATION
    // ----------------------------------------------------------------

    /** Unique identifier of session to be terminated */
    private UUID sessionId;

    /** Unique identifier linking to account */
    private UUID accountId;

    /** Reason for session termination */
    private SessionTerminationReason reason;
    
}